package org.fastprintf.number;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface FloatForm extends NumberForm {

  static FloatForm valueOf(double value) {
    return new DoubleWrapper(value);
  }

  static FloatForm valueOf(BigDecimal value) {
    return new BigDecimalWrapper(value);
  }

  static FloatForm valueOf(BigInteger value) {
    return new BigDecimalWrapper(value, 0);
  }

  boolean isNaN();

  boolean isInfinite();

  FloatLayout generalLayout(int precision);

  FloatLayout scientificLayout(int precision);

  FloatLayout decimalLayout(int precision);

  FloatLayout hexLayout(int precision);
}
