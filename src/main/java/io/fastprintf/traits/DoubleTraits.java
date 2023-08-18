package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class DoubleTraits implements FormatTraits {

  private final double value;

  public DoubleTraits(double value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf((long) value);
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
