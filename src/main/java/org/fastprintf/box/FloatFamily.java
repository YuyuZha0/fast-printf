package org.fastprintf.box;

import java.math.BigDecimal;

public interface FloatFamily {

  static FloatFamily valueOf(double value) {
    return new DoubleBox(value);
  }

  static FloatFamily valueOf(BigDecimal value) {
    return new BigDecimalBox(value);
  }

  int signum();

  boolean isNaN();

  boolean isInfinite();

  FloatLayout generalLayout(int precision);

  FloatLayout scientificLayout(int precision);

  FloatLayout decimalLayout(int precision);

  String toHexString(int precision);

  default boolean isNegative() {
    return signum() < 0;
  }
}
