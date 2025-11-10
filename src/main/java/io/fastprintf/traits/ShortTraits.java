package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class ShortTraits implements FormatTraits {

  private final short value;
  private final RefSlot ref;

  public ShortTraits(short value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static ShortTraits ofPrimitive(short value) {
    return new ShortTraits(value, RefSlot.ofPrimitive());
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(value);
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(value);
  }

  @Override
  public String asString() {
    return Short.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public char asChar() {
    return (char) value;
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
