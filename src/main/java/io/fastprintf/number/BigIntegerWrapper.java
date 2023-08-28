package io.fastprintf.number;

import io.fastprintf.PrintfException;

import java.math.BigInteger;

public final class BigIntegerWrapper implements IntForm {

  private final BigInteger value;
  private final int signum;

  public BigIntegerWrapper(BigInteger value) {
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
    ensureNonNegative();
    return value.toString(16);
  }

  @Override
  public String toOctalString() {
    ensureNonNegative();
    return value.toString(8);
  }

  @Override
  public String toUnsignedDecimalString() {
    ensureNonNegative();
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

  private void ensureNonNegative() {
    if (signum < 0) {
      throw new PrintfException("Negative BigInteger(-%s) cannot be converted to unsigned", value);
    }
  }
}
