package io.fastprintf;

import io.fastprintf.util.Preconditions;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

public final class FormatContext implements Serializable {

  public static final int PRECEDING = Integer.MIN_VALUE;
  public static final int UNSET = -1;
  // A sensible maximum for width or precision to prevent OutOfMemoryErrors.
  // This guards against accidental use of large numbers like timestamps.
  private static final int MAX_WIDTH_OR_PRECISION = 65536;

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

  // Helper method to centralize validation logic.
  private static void checkValue(String name, int value) {
    if (value < 0 && value != UNSET && value != PRECEDING) {
      // This should ideally not be reached if callers are correct,
      // but serves as a strong internal safeguard.
      throw new PrintfException("%s cannot be negative, but was: %s", name, value);
    }
    if (value > MAX_WIDTH_OR_PRECISION) {
      throw new PrintfException(
          "%s %s exceeds the maximum allowed value of %s", name, value, MAX_WIDTH_OR_PRECISION);
    }
  }

  private static void checkWidth(int width) {
    checkValue("Width", width);
  }

  private static void checkPrecision(int precision) {
    checkValue("Precision", precision);
  }

  public static FormatContext create(
      EnumSet<Flag> flags, int width, int precision, DateTimeFormatter dateTimeFormatter) {
    Preconditions.checkNotNull(flags, "flags");
    checkWidth(width);
    checkPrecision(precision);
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
    return create(flagSet, w, p, null);
  }

  public int getWidth() {
    return width;
  }

  public FormatContext setWidth(int newWidth) {
    // Validate the new width before creating the new instance.
    checkWidth(newWidth);
    return new FormatContext(EnumSet.copyOf(flags), newWidth, precision, dateTimeFormatter);
  }

  public boolean isWidthSet() {
    return width != UNSET;
  }

  public int getPrecision() {
    return precision;
  }

  public FormatContext setPrecision(int newPrecision) {
    // Validate the new precision before creating the new instance.
    checkPrecision(newPrecision);
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
