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
    if (value >= 0) {
      return Integer.toString(value);
    }
    if (value > Integer.MIN_VALUE) {
      return Integer.toString(-value);
    }
    return Integer.toString(value).substring(1);
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
