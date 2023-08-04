package org.fastprintf.box;

import org.fastprintf.seq.Seq;

public final class FloatLayout {

  private final Seq mantissa;
  private final Seq exponent;

  FloatLayout(Seq mantissa, Seq exponent) {
    this.mantissa = mantissa;
    this.exponent = exponent;
  }

  public Seq getMantissa() {
    return mantissa;
  }

  public Seq getExponent() {
    return exponent;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (mantissa != null) {
      mantissa.appendTo(builder);
    }
    if (exponent != null) {
      builder.append('e');
      exponent.appendTo(builder);
    }
    return builder.toString();
  }
}
