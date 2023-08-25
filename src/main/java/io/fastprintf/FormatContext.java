package io.fastprintf;

import io.fastprintf.util.Preconditions;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

public final class FormatContext implements Serializable {

  public static final int PRECEDING = Integer.MIN_VALUE;
  public static final int UNSET = -1;

  private static final long serialVersionUID = 460649064794800700L;
  private final EnumSet<Flag> flags;
  private final int width;
  private final int precision;
  private final DateTimeFormatter dateTimeFormatter;

  private FormatContext(
      EnumSet<Flag> flags, int width, int precision, DateTimeFormatter dateTimeFormatter) {
    this.flags = flags;
    this.width = width;
    this.precision = precision;
    this.dateTimeFormatter = dateTimeFormatter;
  }

  public static FormatContext create(
      EnumSet<Flag> flags, int width, int precision, DateTimeFormatter dateTimeFormatter) {
    Preconditions.checkNotNull(flags, "flags");
    return new FormatContext(EnumSet.copyOf(flags), width, precision, dateTimeFormatter);
  }

  public static FormatContext create(String flags) {
    return create(flags, UNSET, UNSET);
  }

  // Helper method for constructing tests.
  public static FormatContext create(String flags, int w, int p) {
    EnumSet<Flag> flagSet = EnumSet.noneOf(Flag.class);
    if (flags != null && !flags.isEmpty()) {
      int length = flags.length();
      for (int i = 0; i < length; i++) {
        flagSet.add(Flag.valueOf(flags.charAt(i)));
      }
    }
    return new FormatContext(flagSet, w, p, null);
  }

  public int getWidth() {
    return width;
  }

  public FormatContext setWidth(int newWidth) {
    return new FormatContext(EnumSet.copyOf(flags), newWidth, precision, dateTimeFormatter);
  }

  public boolean isWidthSet() {
    return width != UNSET;
  }

  public int getPrecision() {
    return precision;
  }

  public FormatContext setPrecision(int newPrecision) {
    return new FormatContext(EnumSet.copyOf(flags), width, newPrecision, dateTimeFormatter);
  }

  public boolean isPrecisionSet() {
    return precision != UNSET;
  }

  public boolean isPrecedingWidth() {
    return width == PRECEDING;
  }

  public boolean isPrecedingPrecision() {
    return precision == PRECEDING;
  }

  public EnumSet<Flag> getFlags() {
    return EnumSet.copyOf(flags);
  }

  public FormatContext addFlag(Flag flag) {
    if (flag == null || flags.contains(flag)) {
      return this;
    }
    FormatContext newContext =
        new FormatContext(EnumSet.copyOf(flags), width, precision, dateTimeFormatter);
    newContext.flags.add(flag);
    return newContext;
  }

  public boolean hasFlag(Flag flag) {
    return flag != null && flags.contains(flag);
  }

  public DateTimeFormatter getDateTimeFormatter() {
    return dateTimeFormatter;
  }

  @Override
  public String toString() {
    return "{" + "flags=" + flags + ", width=" + width + ", precision=" + precision + '}';
  }

  public String toPatternString(Specifier specifier) {
    Preconditions.checkNotNull(specifier, "specifier");
    StringBuilder builder = new StringBuilder();
    builder.append('%');
    for (Flag flag : flags) {
      switch (flag) {
        case ALTERNATE:
          builder.append('#');
          break;
        case PLUS:
          builder.append('+');
          break;
        case LEFT_JUSTIFY:
          builder.append('-');
          break;
        case ZERO_PAD:
          builder.append('0');
          break;
        case LEADING_SPACE:
          builder.append(' ');
          break;
      }
    }
    if (width == PRECEDING) {
      builder.append('*');
    } else if (width != UNSET) {
      builder.append(width);
    }
    if (precision == PRECEDING) {
      builder.append(".*");
    } else if (precision != UNSET) {
      builder.append('.').append(precision);
    }
    builder.append(specifier);
    return builder.toString();
  }
}
