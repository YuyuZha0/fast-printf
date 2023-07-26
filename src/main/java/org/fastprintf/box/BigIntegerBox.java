package org.fastprintf.box;

import java.math.BigInteger;

public final class BigIntegerBox implements IntFamily {

  private final BigInteger value;
  private final int signum;

  public BigIntegerBox(BigInteger value) {
    this.value = value.abs();
    this.signum = value.signum();
  }

  @Override
  public int signum() {
    return signum;
  }

  @Override
  public String toDecimalString() {
    return value.toString();
  }

  @Override
  public String toHexString() {
    return value.toString(16);
  }

  @Override
  public String toOctalString() {
    return value.toString(8);
  }

  @Override
  public String toUnsignedDecimalString() {
    return value.toString();
  }

  @Override
  public String toString() {
    if (signum >= 0) {
      return value.toString();
    } else {
      return "-" + value.toString();
    }
  }
}
