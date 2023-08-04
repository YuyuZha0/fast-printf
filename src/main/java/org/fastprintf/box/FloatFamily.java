package org.fastprintf.box;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface FloatFamily extends NumberFamily {

  static FloatFamily valueOf(double value) {
    return new DoubleBox(value);
  }

  static FloatFamily valueOf(BigDecimal value) {
    return new BigDecimalBox(value);
  }

  static FloatFamily valueOf(BigInteger value) {
    return new BigDecimalBox(value, 0);
  }

  boolean isNaN();

  boolean isInfinite();

  FloatLayout generalLayout(int precision);

  FloatLayout scientificLayout(int precision);

  FloatLayout decimalLayout(int precision);

  FloatLayout hexLayout(int precision);
}
