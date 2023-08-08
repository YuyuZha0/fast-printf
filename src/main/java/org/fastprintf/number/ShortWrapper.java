package org.fastprintf.number;

public final class ShortWrapper implements IntForm {

  private final short value;

  public ShortWrapper(short value) {
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
