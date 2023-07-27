package org.fastprintf.box;

import java.math.BigDecimal;

public final class BigDecimalBox implements FloatFamily {

  private final BigDecimal value;
  private final int signum;

  public BigDecimalBox(BigDecimal value) {
    this.value = value.abs();
    this.signum = value.signum();
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
    return null;
  }

  @Override
  public FloatLayout scientificLayout(int precision) {
    return null;
  }

  @Override
  public FloatLayout decimalLayout(int precision) {
    return null;
  }

  @Override
  public String toHexString(int precision) {
    return null;
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
