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

  String toGeneralString(int precision);

  String toScientificString(int precision);

  String toDecimalString(int precision);

  String toHexString(int precision);
}
