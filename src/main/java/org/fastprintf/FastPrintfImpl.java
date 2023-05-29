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

  private final List<Appender> appenders;

  private FastPrintfImpl(List<Appender> appenders) {
    this.appenders = appenders;
  }

  static FastPrintfImpl compile(String format) {
    Compiler compiler = new Compiler(format);
    compiler.compile();
    return new FastPrintfImpl(new ArrayList<>(compiler.getAppenders()));
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
      int totalLength = 0;
      for (Seq seq : collect) {
        totalLength += seq.length();
      }
      StringBuilder stringBuilder = (StringBuilder) builder;
      int oldLength = stringBuilder.length();
      stringBuilder.ensureCapacity(oldLength + totalLength);
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
}
