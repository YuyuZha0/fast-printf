package org.fastprintf.box;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class BigDecimalBox implements FloatFamily {

  private final BigInteger unscaledValue;
  private final int scale;
  private final int signum;

  public BigDecimalBox(BigDecimal value) {
    this(value.unscaledValue(), value.scale());
  }

  public BigDecimalBox(BigInteger unscaledValue, int scale) {
    this.unscaledValue = unscaledValue.abs();
    this.scale = scale;
    this.signum = unscaledValue.signum();
  }

  @Override
  public int signum() {
    return signum;
  }

  @Override
  public boolean isNaN() {
    return false;
  }

  @Override
  public boolean isInfinite() {
    return false;
  }

  @Override
  public FloatLayout generalLayout(int precision) {
    // TODO
    return null;
  }

  @Override
  public FloatLayout scientificLayout(int precision) {
    // TODO
    return null;
  }

  @Override
  public FloatLayout decimalLayout(int precision) {
    // TODO
    return null;
  }

  @Override
  public FloatLayout hexLayout(int precision) {
    // TODO
    return null;
  }

  @Override
  public String toString() {
    if (signum >= 0) {
      return new BigDecimal(unscaledValue, scale).toString();
    } else {
      return "-" + new BigDecimal(unscaledValue, scale);
    }
  }
}
