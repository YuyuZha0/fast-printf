package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class DoubleTraits implements FormatTraits {

  private final double value;
  private final RefSlot ref;

  public DoubleTraits(double value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static DoubleTraits ofPrimitive(double value) {
    return new DoubleTraits(value, RefSlot.ofPrimitive());
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
  public RefSlot ref() {
    return ref;
  }

  @Override
  public Object asObject() {
    return ref.isPrimitive() ? value : ref.get();
  }
}
