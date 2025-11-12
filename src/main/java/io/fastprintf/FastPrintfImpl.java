package io.fastprintf;

import io.fastprintf.appender.Appender;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.IntFunction;

/** GRAMMAR: %[flags][width][.precision]specifier */
final class FastPrintfImpl implements FastPrintf {

  private static final int STRING_BUILDER_MAX_RETAINED_CAPACITY = 65536;

  private final Appender[] appenders;
  private final int stringBuilderInitialCapacity;
  private final ThreadLocal<StringBuilder> threadLocalBuilder;
  private final IntFunction<StringBuilder> stringBuilderFactory;

  private FastPrintfImpl(
      Appender[] appenders, int stringBuilderInitialCapacity, boolean enableThreadLocalCache) {
    this.appenders = appenders;
    this.stringBuilderInitialCapacity = stringBuilderInitialCapacity;
    if (enableThreadLocalCache) {
      // Initialize with a default-sized builder to avoid startup costs for every thread.
      // The user's initial capacity will be applied on the first format call.
      this.threadLocalBuilder = ThreadLocal.withInitial(StringBuilder::new);

      this.stringBuilderFactory =
          requiredCapacity -> {
            StringBuilder builder = threadLocalBuilder.get();
            int currentCapacity = builder.capacity();

            // This is the core logic:
            // We only reset the builder if it has grown unnecessarily large.
            // "Unnecessarily large" means:
            // 1. Its current capacity exceeds our maximum retention limit.
            // AND
            // 2. The capacity required for THIS specific call is within that limit.
            // This prevents churn when the user intentionally sets a large initial capacity.
            if (currentCapacity > STRING_BUILDER_MAX_RETAINED_CAPACITY
                && requiredCapacity <= STRING_BUILDER_MAX_RETAINED_CAPACITY) {

              // The buffer is too big and we don't need it this time.
              // Create a new, reasonably-sized builder and replace the old one.
              builder = new StringBuilder(requiredCapacity);
              threadLocalBuilder.set(builder);

            } else {
              // In all other cases, we reuse the existing builder:
              // - If it's within the size limit.
              // - If it's large, but the user is asking for a large buffer anyway (avoids churn).
              builder.setLength(0);
              builder.ensureCapacity(requiredCapacity);
            }
            return builder;
          };
    } else {
      this.threadLocalBuilder = null;
      this.stringBuilderFactory = StringBuilder::new;
    }
  }

  static FastPrintfImpl compile(String format) {
    Compiler compiler = new Compiler(format);
    compiler.compile();
    int sourceLength =
        Math.max(format.length(), 11); // To align with StringBuilder default capacity
    return new FastPrintfImpl(
        compiler.getAppenders().toArray(new Appender[0]),
        Math.addExact(sourceLength, sourceLength >> 1), // 1.5x format length as initial capacity
        false);
  }

  @Override
  public String format(Args args) {
    Preconditions.checkNotNull(args, "args");
    Iterator<FormatTraits> iterator = args.iterator();
    StringBuilder builder = stringBuilderFactory.apply(stringBuilderInitialCapacity);
    for (Appender appender : appenders) {
      appender.append(seq -> seq.appendTo(builder), iterator);
    }
    return builder.toString();
  }

  @Override
  public <T extends Appendable> T format(T builder, Args args) {
    Preconditions.checkNotNull(builder, "builder");
    Preconditions.checkNotNull(args, "args");
    Iterator<FormatTraits> iterator = args.iterator();

    if (builder instanceof StringBuilder) {
      StringBuilder stringBuilder = (StringBuilder) builder;
      for (Appender appender : appenders) {
        appender.append(seq -> seq.appendTo(stringBuilder), iterator);
      }
      return builder;
    }
    for (Appender appender : appenders) {
      appender.append(
          seq -> {
            try {
              seq.appendTo(builder);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          },
          iterator);
    }
    return builder;
  }

  @Override
  public FastPrintfImpl enableThreadLocalCache() {
    if (threadLocalBuilder != null) {
      return this;
    }
    return new FastPrintfImpl(
        Arrays.copyOf(appenders, appenders.length), stringBuilderInitialCapacity, true);
  }

  @Override
  public FastPrintfImpl setStringBuilderInitialCapacity(int capacity) {
    Preconditions.checkArgument(capacity > 0, "capacity must be positive");
    if (this.stringBuilderInitialCapacity == capacity) {
      return this;
    }
    return new FastPrintfImpl(
        Arrays.copyOf(appenders, appenders.length), capacity, threadLocalBuilder != null);
  }
}
