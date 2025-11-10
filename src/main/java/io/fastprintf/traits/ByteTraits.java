package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class ByteTraits implements FormatTraits {

  private final byte value;
  private final RefSlot ref;

  public ByteTraits(byte value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static ByteTraits ofPrimitive(byte value) {
    return new ByteTraits(value, RefSlot.ofPrimitive());
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
    return Byte.toString(value);
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
