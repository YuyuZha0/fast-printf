package org.fastprintf;

import java.util.EnumSet;
import java.util.Objects;

public final class FormatContext {

  public static final int PRECEDING = Integer.MIN_VALUE;
  public static final int UNSET = -1;
  private final EnumSet<Flag> flags;
  private final boolean precedingWidth;
  private final boolean precedingPrecision;
  private int width = UNSET;
  private int precision = UNSET;

  public FormatContext(EnumSet<Flag> flags, boolean precedingWidth, boolean precedingPrecision) {
    this.flags = EnumSet.copyOf(flags);
    this.precedingWidth = precedingWidth;
    this.precedingPrecision = precedingPrecision;
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
    FormatContext context = new FormatContext(flagSet, w == PRECEDING, p == PRECEDING);
    if (w >= 0) {
      context.setWidth(w);
    }
    if (p >= 0) {
      context.setPrecision(p);
    }
    return context;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public boolean isWidthSet() {
    return width != UNSET;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public boolean isPrecisionSet() {
    return precision != UNSET;
  }

  public boolean isPrecedingWidth() {
    return precedingWidth;
  }

  public boolean isPrecedingPrecision() {
    return precedingPrecision;
  }

  public EnumSet<Flag> getFlags() {
    return EnumSet.copyOf(flags);
  }

  public void addFlag(Flag flag) {
    flags.add(flag);
  }

  public boolean hasFlag(Flag flag) {
    return flag != null && flags.contains(flag);
  }

  @Override
  public String toString() {
    return "{" + "flags=" + flags + ", width=" + width + ", precision=" + precision + '}';
  }

  public String toPatternString(Specifier specifier) {
    Objects.requireNonNull(specifier, "specifier is null");
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
    if (width == FormatContext.PRECEDING) {
      builder.append('*');
    } else if (width != FormatContext.UNSET) {
      builder.append(width);
    }
    if (precision == FormatContext.PRECEDING) {
      builder.append(".*");
    } else if (precision != FormatContext.UNSET) {
      builder.append('.').append(precision);
    }
    builder.append(specifier);
    return builder.toString();
  }
}
