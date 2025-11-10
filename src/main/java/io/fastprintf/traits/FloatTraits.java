package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class FloatTraits implements FormatTraits {

  private final float value;
  private final RefSlot ref;

  public FloatTraits(float value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static FloatTraits ofPrimitive(float value) {
    return new FloatTraits(value, RefSlot.ofPrimitive());
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
  public RefSlot ref() {
    return ref;
  }

  @Override
  public Object asObject() {
    return ref.isPrimitive() ? value : ref.get();
  }
}
