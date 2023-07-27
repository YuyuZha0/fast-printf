package org.fastprintf.box;

import sun.misc.FormattedFloatingDecimal;

public final class DoubleBox implements FloatFamily {

  private final double value;

  public DoubleBox(double value) {
    this.value = value;
  }

  @Override
  public int signum() {
    return Double.compare(value, 0D);
  }

  @Override
  public boolean isNaN() {
    return Double.isNaN(value);
  }

  @Override
  public boolean isInfinite() {
    return Double.isInfinite(value);
  }

  private FloatLayout toLayout(int precision, FormattedFloatingDecimal.Form form) {
    FormattedFloatingDecimal fd =
        FormattedFloatingDecimal.valueOf(Math.abs(value), precision, form);
    return new FloatLayout(fd.getMantissa(), fd.getExponent(), fd.getExponentRounded());
  }

  @Override
  public FloatLayout generalLayout(int precision) {
    if (value == 0D) {
      return new FloatLayout(new char[] {'0'}, null, 0);
    }
    return toLayout(precision, FormattedFloatingDecimal.Form.GENERAL);
  }

  @Override
  public FloatLayout scientificLayout(int precision) {
    if (value == 0D) {
      return new FloatLayout(new char[] {'0'}, new char[] {'+', '0', '0'}, 0);
    }
    return toLayout(precision, FormattedFloatingDecimal.Form.SCIENTIFIC);
  }

  @Override
  public FloatLayout decimalLayout(int precision) {
    return toLayout(precision, FormattedFloatingDecimal.Form.DECIMAL_FLOAT);
  }

  @Override
  public String toHexString(int precision) {
    return String.format("%." + precision + "a", value);
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }
}
