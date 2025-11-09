package io.fastprintf;

import io.fastprintf.appender.Appender;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.IntFunction;

/** GRAMMAR: %[flags][width][.precision]specifier */
final class FastPrintfImpl implements FastPrintf {

  private final Appender[] appenders;
  private final int stringBuilderInitialCapacity;
  private final ThreadLocal<StringBuilder> threadLocalBuilder;
  private final IntFunction<StringBuilder> stringBuilderFactory;

  private FastPrintfImpl(
      Appender[] appenders, int stringBuilderInitialCapacity, boolean enableThreadLocalCache) {
    this.appenders = appenders;
    this.stringBuilderInitialCapacity = stringBuilderInitialCapacity;
    if (enableThreadLocalCache) {
      this.threadLocalBuilder = ThreadLocal.withInitial(StringBuilder::new);
      this.stringBuilderFactory =
          len -> {
            StringBuilder builder = threadLocalBuilder.get();
            builder.setLength(0);
            builder.ensureCapacity(len);
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
  public FastPrintf enableThreadLocalCache() {
    if (threadLocalBuilder != null) {
      return this;
    }
    return new FastPrintfImpl(appenders.clone(), stringBuilderInitialCapacity, true);
  }
}
