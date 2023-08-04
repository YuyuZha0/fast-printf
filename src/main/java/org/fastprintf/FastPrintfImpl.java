package org.fastprintf;

import org.fastprintf.appender.Appender;
import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/** GRAMMAR: %[flags][width][.precision]specifier */
final class FastPrintfImpl implements FastPrintf {

  private final Appender[] appenders;
  private final ThreadLocal<StringBuilder> threadLocalBuilder;

  private FastPrintfImpl(Appender[] appenders, boolean enableThreadLocalCache) {
    this.appenders = appenders;
    if (enableThreadLocalCache) {
      this.threadLocalBuilder = ThreadLocal.withInitial(StringBuilder::new);
    } else {
      this.threadLocalBuilder = null;
    }
  }

  static FastPrintfImpl compile(String format) {
    Compiler compiler = new Compiler(format);
    compiler.compile();
    return new FastPrintfImpl(compiler.getAppenders().toArray(new Appender[0]), false);
  }

  private static int precomputeLength(List<Seq> collect) {
    int totalLength = 0;
    for (Seq seq : collect) {
      totalLength += seq.length();
    }
    return totalLength;
  }

  @Override
  public String format(Args args) {
    Objects.requireNonNull(args, "args");
    Iterator<FormatTraits> iterator = args.iterator();
    List<Seq> collect = new ArrayList<>();
    for (Appender appender : appenders) {
      appender.append(collect, iterator);
    }
    int totalLength = precomputeLength(collect);
    StringBuilder builder = getStringBuilder(totalLength);
    for (Seq seq : collect) {
      seq.appendTo(builder);
    }
    return builder.toString();
  }

  private StringBuilder getStringBuilder(int totalLength) {
    if (threadLocalBuilder != null) {
      StringBuilder builder = threadLocalBuilder.get();
      builder.setLength(0);
      builder.ensureCapacity(totalLength);
      return builder;
    }
    return new StringBuilder(totalLength);
  }

  @Override
  public <T extends Appendable> T format(T builder, Args args) {
    Objects.requireNonNull(builder, "builder");
    Objects.requireNonNull(args, "args");
    Iterator<FormatTraits> iterator = args.iterator();
    List<Seq> collect = new ArrayList<>();
    for (Appender appender : appenders) {
      appender.append(collect, iterator);
    }
    if (builder instanceof StringBuilder) {
      int totalLength = precomputeLength(collect);
      StringBuilder stringBuilder = (StringBuilder) builder;
      int oldLength = stringBuilder.length();
      stringBuilder.ensureCapacity(oldLength + totalLength);
      for (Seq seq : collect) {
        seq.appendTo(stringBuilder);
      }
      return builder;
    }
    try {
      for (Seq seq : collect) {
        seq.appendTo(builder);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return builder;
  }

  @Override
  public FastPrintf enableThreadLocalCache() {
    if (threadLocalBuilder != null) {
      return this;
    }
    return new FastPrintfImpl(appenders.clone(), true);
  }

  @Override
  protected void finalize() throws Throwable {
    if (threadLocalBuilder != null) {
      threadLocalBuilder.remove();
    }
    super.finalize();
  }
}
