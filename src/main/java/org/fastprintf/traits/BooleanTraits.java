package org.fastprintf.traits;

import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;

public final class BooleanTraits implements FormatTraits {

  private final boolean value;

  public BooleanTraits(boolean value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(asInt());
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(asInt());
  }

  @Override
  public String asString() {
    return Boolean.toString(value);
  }

  @Override
  public int asInt() {
    return value ? 1 : 0;
  }

  @Override
  public Object value() {
    return value;
  }
}
