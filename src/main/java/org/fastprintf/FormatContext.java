package org.fastprintf;

import java.util.EnumSet;

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

  public boolean hasFlag(Flag flag) {
    return flag != null && flags.contains(flag);
  }

  @Override
  public String toString() {
    return "{" + "flags=" + flags + ", width=" + width + ", precision=" + precision + '}';
  }
}
