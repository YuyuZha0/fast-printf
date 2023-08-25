package io.fastprintf;

import io.fastprintf.appender.Appender;
import io.fastprintf.appender.DefaultAppender;
import io.fastprintf.appender.FixedStringAppender;
import io.fastprintf.util.Preconditions;
import io.fastprintf.util.Utils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

final class Compiler {

  private final String source;
  private final List<Appender> appenders = new ArrayList<>();
  private int lookahead = 0;

  Compiler(String source) {
    this.source = Preconditions.checkNotNull(source, "source");
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
    // %[flags][width][.precision][{date-time-formatter}]specifier
    EnumSet<Flag> flags = flags();
    int width = width();
    int precision = precision();
    DateTimeFormatter dateTimeFormatter = dateTimeFormatter();
    Specifier specifier = specifier();
    FormatContext context = FormatContext.create(flags, width, precision, dateTimeFormatter);
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
      if (c == '.' || Utils.isNotDigit(c)) {
        break;
      }
      lookahead++;
      checkSource();
    }
    String w = source.substring(start, lookahead);
    if (w.isEmpty()) {
      return FormatContext.UNSET;
    }
    try {
      return Integer.parseInt(w);
    } catch (NumberFormatException e) {
      throw new PrintfSyntaxException("Invalid width", source, lookahead);
    }
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
      if (Utils.isNotDigit(c)) {
        break;
      }
      lookahead++;
      checkSource();
    }
    String p = source.substring(start, lookahead);
    if (p.isEmpty()) {
      return 0;
      // throw new PrintfSyntaxException("Invalid precision", source, lookahead);
    }
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      throw new PrintfSyntaxException("Invalid precision", source, lookahead);
    }
  }

  private DateTimeFormatter dateTimeFormatter() {
    checkSource();
    if (source.charAt(lookahead) != '{') {
      return null;
    }
    int start = lookahead + 1;
    int end = source.indexOf('}', start + 1);
    if (end == -1) {
      throw new PrintfSyntaxException("Enclosed \"{}\" for date time pattern", source, lookahead);
    }
    String pattern = source.substring(start, end);
    lookahead = end + 1;
    try {
      return DateTimeFormatter.ofPattern(pattern);
    } catch (IllegalArgumentException e) {
      throw new PrintfSyntaxException("Invalid date time pattern", source, lookahead);
    }
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
