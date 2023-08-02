package org.fastprintf.box;

import org.fastprintf.seq.Seq;

import java.io.IOException;

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
      try {
        mantissa.appendTo(builder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    if (exponent != null) {
      builder.append('e');
      try {
        exponent.appendTo(builder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return builder.toString();
  }
}
