package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class DoubleTraits implements FormatTraits {

  private final double value;

  public DoubleTraits(double value) {
    this.value = value;
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(Math.round(value));
  }

  @Override
  public FloatFamily asFloatFamily() {
    return FloatFamily.valueOf(value);
  }

  @Override
  public String asString() {
    return Double.toString(value);
  }

  @Override
  public int asInt() {
    return (int) Math.round(value);
  }

  @Override
  public Object value() {
    return value;
  }
}
