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

  @Override
  public String toGeneralString(int precision) {
    // TODO: Implement this method
    return String.format("%." + precision + "g", value);
  }

  @Override
  public String toScientificString(int precision) {
    return String.format("%." + precision + "e", value);
  }

  @Override
  public String toDecimalString(int precision) {
    FormattedFloatingDecimal fd =
            FormattedFloatingDecimal.valueOf(value, precision, FormattedFloatingDecimal.Form.GENERAL);
    return "";
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
