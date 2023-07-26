package org.fastprintf.box;

public final class ShortBox implements IntFamily {

  private final short value;

  public ShortBox(short value) {
    this.value = value;
  }

  @Override
  public int signum() {
    return Integer.signum(value);
  }

  @Override
  public String toDecimalString() {
    return Integer.toString(Math.abs(value));
  }

  @Override
  public String toHexString() {
    return Integer.toHexString(Short.toUnsignedInt(value));
  }

  @Override
  public String toOctalString() {
    return Integer.toOctalString(Short.toUnsignedInt(value));
  }

  @Override
  public String toUnsignedDecimalString() {
    return Integer.toUnsignedString(Short.toUnsignedInt(value));
  }

  @Override
  public String toString() {
    return Short.toString(value);
  }
}
