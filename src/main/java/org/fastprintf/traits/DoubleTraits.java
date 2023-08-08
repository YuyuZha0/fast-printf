package org.fastprintf.traits;

import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;

public final class DoubleTraits implements FormatTraits {

  private final double value;

  public DoubleTraits(double value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(Math.round(value));
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(value);
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
