package org.fastprintf.box;

public final class IntBox implements IntFamily {

  private final int value;

  public IntBox(int value) {
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
    return Integer.toHexString(value);
  }

  @Override
  public String toOctalString() {
    return Integer.toOctalString(value);
  }

  @Override
  public String toUnsignedDecimalString() {
    return Integer.toUnsignedString(value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
