package org.fastprintf.number;

public final class LongWrapper implements IntForm {

  private final long value;

  public LongWrapper(long value) {
    this.value = value;
  }

  @Override
  public int signum() {
    return Long.signum(value);
  }

  @Override
  public String toDecimalString() {
    if (value >= 0) {
      return Long.toString(value);
    }
    if (value > Long.MIN_VALUE) {
      return Long.toString(-value);
    }
    return "9223372036854775808";
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
