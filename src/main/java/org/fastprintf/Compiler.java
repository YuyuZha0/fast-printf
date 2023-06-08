package org.fastprintf;

import org.fastprintf.appender.Appender;
import org.fastprintf.appender.DefaultAppender;
import org.fastprintf.appender.FixedStringAppender;
import org.fastprintf.util.Utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

final class Compiler {

  private final String source;
  private final List<Appender> appenders = new ArrayList<>();
  private int lookahead = 0;

  Compiler(String source) {
    Objects.requireNonNull(source, "source is null");
    this.source = source;
  }

  void compile() {
    recursiveDecrease();
  }

  private boolean endOfSource() {
    return lookahead >= source.length();
  }

  private void recursiveDecrease() {
    if (endOfSource()) return;
    char c = source.charAt(lookahead);
    if (c == '%') {
      lookahead++;
      forPattern();
    } else {
      forFixedString();
    }
  }

  private void forPattern() {
    checkSource();
    char c = source.charAt(lookahead);
    if (c == '%') {
      lookahead++;
      appenders.add(new FixedStringAppender("%"));
      recursiveDecrease();
      return;
    }
    // %[flags][width][.precision]specifier */
    EnumSet<Flag> flags = flags();
    int width = width();
    int precision = precision();
    Specifier specifier = specifier();
    FormatContext context =
        new FormatContext(
            flags, width == FormatContext.PRECEDING, precision == FormatContext.PRECEDING);
    context.setWidth(width);
    context.setPrecision(precision);
    appenders.add(new DefaultAppender(specifier, context));
    recursiveDecrease();
  }

  private EnumSet<Flag> flags() {
    EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
    while (true) {
      checkSource();
      Flag flag = Flag.valueOf(source.charAt(lookahead));
      if (flag == null) {
        break;
      }
      flags.add(flag);
      lookahead++;
    }
    return flags;
  }

  private void checkSource() {
    if (endOfSource()) {
      throw new PrintfSyntaxException("Unexpected end of source", source, lookahead);
    }
  }

  private int width() {
    checkSource();
    final int start = lookahead;
    if (source.charAt(lookahead) == '*') {
      lookahead++;
      return FormatContext.PRECEDING;
    }
    while (true) {
      char c = source.charAt(lookahead);
      if (c == '.' || !Utils.isDigit(c)) {
        break;
      }
      lookahead++;
      checkSource();
    }
    String w = source.substring(start, lookahead);
    if (w.isEmpty()) {
      return FormatContext.UNSET;
    }
    return Integer.parseInt(w);
  }

  private int precision() {
    checkSource();
    if (source.charAt(lookahead) != '.') {
      return FormatContext.UNSET;
    }
    lookahead++;
    checkSource();
    if (source.charAt(lookahead) == '*') {
      lookahead++;
      return FormatContext.PRECEDING;
    }
    checkSource();
    final int start = lookahead;
    while (true) {
      char c = source.charAt(lookahead);
      if (!Utils.isDigit(c)) {
        break;
      }
      lookahead++;
      checkSource();
    }
    String p = source.substring(start, lookahead);
    if (p.isEmpty()) {
      throw new PrintfSyntaxException("Invalid precision", source, lookahead);
    }
    return Integer.parseInt(p);
  }

  private Specifier specifier() {
    checkSource();
    Specifier specifier = Specifier.valueOf(source.charAt(lookahead));
    if (specifier == null) {
      throw new PrintfSyntaxException("Invalid specifier", source, lookahead);
    }
    lookahead++;
    return specifier;
  }

  private void forFixedString() {
    int length = source.length();
    int start = lookahead;
    boolean meetPercent = false;
    while (lookahead < length) {
      char c = source.charAt(lookahead);
      if (c == '%') {
        meetPercent = true;
        break;
      }
      lookahead++;
    }
    appenders.add(new FixedStringAppender(source.substring(start, lookahead)));
    if (meetPercent) {
      lookahead++;
      forPattern();
    }
  }

  List<Appender> getAppenders() {
    return appenders;
  }
}
