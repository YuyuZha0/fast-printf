package org.fastprintf.box;

import java.math.BigDecimal;

public interface FloatFamily extends NumberFamily {

  static FloatFamily valueOf(double value) {
    return new DoubleBox(value);
  }

  static FloatFamily valueOf(BigDecimal value) {
    return new BigDecimalBox(value);
  }

  boolean isNaN();

  boolean isInfinite();

  FloatLayout generalLayout(int precision);

  FloatLayout scientificLayout(int precision);

  FloatLayout decimalLayout(int precision);

  FloatLayout hexLayout(int precision);
}
