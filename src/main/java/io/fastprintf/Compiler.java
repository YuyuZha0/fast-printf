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
    checkSource("Format string terminates in the middle of a format specifier");
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
    while (!endOfSource()) {
      Flag flag = Flag.valueOf(source.charAt(lookahead));
      if (flag == null) {
        break;
      }
      if (flags.contains(flag)) {
        throw new PrintfSyntaxException(
            "Duplicate flag '" + flag + "'", source, lookahead);
      }
      flags.add(flag);
      lookahead++;
    }
    return flags;
  }

  private void checkSource(String message) {
    if (endOfSource()) {
      throw new PrintfSyntaxException(message, source, lookahead);
    }
  }

  private int width() {
    checkSource("Format string lacks width or specifier");
    final int start = lookahead;
    if (source.charAt(lookahead) == '*') {
      lookahead++;
      return FormatContext.PRECEDING;
    }
    while (!endOfSource()) {
      char c = source.charAt(lookahead);
      if (Utils.isNotDigit(c)) {
        break;
      }
      lookahead++;
    }
    String w = source.substring(start, lookahead);
    if (w.isEmpty()) {
      return FormatContext.UNSET;
    }
    try {
      return Integer.parseInt(w);
    } catch (NumberFormatException e) {
      throw new PrintfSyntaxException("Invalid width: '" + w + "'", source, start);
    }
  }

  private int precision() {
    if (endOfSource() || source.charAt(lookahead) != '.') {
      return FormatContext.UNSET;
    }
    lookahead++; // Skip '.'
    checkSource("Format string terminates after '.'");

    if (source.charAt(lookahead) == '*') {
      lookahead++;
      return FormatContext.PRECEDING;
    }

    final int start = lookahead;
    while (!endOfSource()) {
      char c = source.charAt(lookahead);
      if (Utils.isNotDigit(c)) {
        break;
      }
      lookahead++;
    }
    String p = source.substring(start, lookahead);
    if (p.isEmpty()) {
      // e.g. "%.s"
      return 0;
    }
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      throw new PrintfSyntaxException("Invalid precision: '" + p + "'", source, start);
    }
  }

  private DateTimeFormatter dateTimeFormatter() {
    if (endOfSource() || source.charAt(lookahead) != '{') {
      return null;
    }
    int patternStart = lookahead + 1;

    // Robustly find the closing brace, respecting single quotes.
    int scanPos = patternStart;
    boolean inQuote = false;
    int patternEnd = -1;

    while (scanPos < source.length()) {
      char c = source.charAt(scanPos);
      if (c == '\'') {
        // Handle escaped single quote ''
        if (scanPos + 1 < source.length() && source.charAt(scanPos + 1) == '\'') {
          scanPos++; // Skip the second quote so it doesn't toggle the state
        } else {
          inQuote = !inQuote; // Toggle quote state
        }
      } else if (c == '}' && !inQuote) {
        patternEnd = scanPos; // Found the unquoted, matching brace
        break;
      }
      scanPos++;
    }

    if (patternEnd == -1) {
      throw new PrintfSyntaxException(
          "Unclosed date/time pattern starting at index " + lookahead, source, lookahead);
    }

    String pattern = source.substring(patternStart, patternEnd);
    if (pattern.isEmpty()) {
      throw new PrintfSyntaxException("Empty date/time pattern", source, patternStart);
    }

    lookahead = patternEnd + 1;

    try {
      return DateTimeFormatter.ofPattern(pattern);
    } catch (IllegalArgumentException e) {
      throw new PrintfSyntaxException(
          "Invalid date/time pattern: '" + pattern + "'", source, patternStart, e);
    }
  }

  private Specifier specifier() {
    checkSource("Format string lacks specifier");
    char c = source.charAt(lookahead);
    Specifier specifier = Specifier.valueOf(c);
    if (specifier == null) {
      throw new PrintfSyntaxException(
          "Unknown format conversion specifier '" + c + "'", source, lookahead);
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
