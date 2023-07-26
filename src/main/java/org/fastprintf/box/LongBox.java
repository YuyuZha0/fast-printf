package org.fastprintf.box;

public final class LongBox implements IntFamily {

  private final long value;

  public LongBox(long value) {
    this.value = value;
  }

  @Override
  public int signum() {
    return Long.signum(value);
  }

  @Override
  public String toDecimalString() {
    return Long.toString(Math.abs(value));
  }

  @Override
  public String toHexString() {
    return Long.toHexString(value);
  }

  @Override
  public String toOctalString() {
    return Long.toOctalString(value);
  }

  @Override
  public String toUnsignedDecimalString() {
    return Long.toUnsignedString(value);
  }

  @Override
  public String toString() {
    return Long.toString(value);
  }
}
