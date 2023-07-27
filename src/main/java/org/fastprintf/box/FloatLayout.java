package org.fastprintf.box;

public final class FloatLayout {

  private final char[] mantissa;
  private final char[] exponent;

  private final int exponentRounded;

  FloatLayout(char[] mantissa, char[] exponent, int exponentRounded) {
    this.mantissa = mantissa;
    this.exponent = exponent;
    this.exponentRounded = exponentRounded;
  }

  public char[] getMantissa() {
    return mantissa;
  }

  public char[] getExponent() {
    return exponent;
  }

  public int getExponentRounded() {
    return exponentRounded;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (mantissa != null) {
      builder.append(mantissa);
    }
    if (exponent != null) {
      builder.append('e');
      builder.append(exponent);
    }
    return builder.toString();
  }
}
