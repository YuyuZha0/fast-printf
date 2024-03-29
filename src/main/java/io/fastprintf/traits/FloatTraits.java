package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class FloatTraits implements FormatTraits {

  private final float value;

  public FloatTraits(float value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf((int) value);
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(value);
  }

  @Override
  public String asString() {
    return Float.toString(value);
  }

  @Override
  public int asInt() {
    return Math.round(value);
  }

  @Override
  public Object value() {
    return value;
  }
}
