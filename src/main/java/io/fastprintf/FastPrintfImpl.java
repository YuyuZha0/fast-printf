package io.fastprintf;

import io.fastprintf.appender.Appender;
import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

/** GRAMMAR: %[flags][width][.precision]specifier */
final class FastPrintfImpl implements FastPrintf {

  private static final int STRING_BUILDER_MAX_RETAINED_CAPACITY = 65536;

  private final Appender[] appenders;
  private final int stringBuilderInitialCapacity;
  private final ThreadLocal<CachedBuilder> threadLocalBuilder;

  private FastPrintfImpl(
      Appender[] appenders, int stringBuilderInitialCapacity, boolean enableThreadLocalCache) {
    this.appenders = appenders;
    this.stringBuilderInitialCapacity = stringBuilderInitialCapacity;
    this.threadLocalBuilder =
        enableThreadLocalCache ? ThreadLocal.withInitial(CachedBuilder::new) : null;
  }

  private static final class CachedBuilder {
    private StringBuilder builder = new StringBuilder();
    private boolean inUse;

    private StringBuilder acquire(int requiredCapacity) {
      if (builder.capacity() > STRING_BUILDER_MAX_RETAINED_CAPACITY
          && requiredCapacity <= STRING_BUILDER_MAX_RETAINED_CAPACITY) {
        builder = new StringBuilder(requiredCapacity);
      } else {
        builder.setLength(0);
        builder.ensureCapacity(requiredCapacity);
      }
      return builder;
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
    CachedBuilder cached = threadLocalBuilder == null ? null : threadLocalBuilder.get();
    // Object.toString() and custom traits can call the same formatter recursively.
    boolean reuse = cached != null && !cached.inUse;
    StringBuilder builder =
        reuse
            ? cached.acquire(stringBuilderInitialCapacity)
            : new StringBuilder(stringBuilderInitialCapacity);
    if (reuse) cached.inUse = true;
    try {
      // Keep this loop local: forwarding to the Appendable overload added 24 B/op in the
      // JDK 21 integer benchmark.
      Iterator<FormatTraits> iterator = args.iterator();
      Consumer<Seq> consumer = seq -> seq.appendTo(builder);
      for (Appender appender : appenders) {
        appender.append(consumer, iterator);
      }
      return builder.toString();
    } finally {
      if (reuse) cached.inUse = false;
    }
  }

  @Override
  public <T extends Appendable> T format(T builder, Args args) {
    Preconditions.checkNotNull(builder, "builder");
    Preconditions.checkNotNull(args, "args");
    Iterator<FormatTraits> iterator = args.iterator();

    if (builder instanceof StringBuilder) {
      StringBuilder stringBuilder = (StringBuilder) builder;
      Consumer<Seq> consumer = seq -> seq.appendTo(stringBuilder);
      for (Appender appender : appenders) {
        appender.append(consumer, iterator);
      }
      return builder;
    }
    Consumer<Seq> consumer =
        seq -> {
          try {
            seq.appendTo(builder);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        };
    for (Appender appender : appenders) {
      appender.append(consumer, iterator);
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
