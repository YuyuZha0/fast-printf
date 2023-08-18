package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class BooleanTraits implements FormatTraits {

  private static final FormatTraits TRUE = new BooleanTraits(true);
  private static final FormatTraits FALSE = new BooleanTraits(false);

  private final boolean value;

  private BooleanTraits(boolean value) {
    this.value = value;
  }

  public static FormatTraits valueOf(boolean value) {
    return value ? TRUE : FALSE;
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
