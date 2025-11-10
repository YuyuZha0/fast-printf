package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.util.Utils;
import java.time.temporal.TemporalAccessor;

public final class IntTraits implements FormatTraits {

  private final int value;
  private final RefSlot ref;

  public IntTraits(int value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static IntTraits ofPrimitive(int value) {
    return new IntTraits(value, RefSlot.ofPrimitive());
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
    return Integer.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public RefSlot ref() {
    return ref;
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return Utils.longToInstant(Integer.toUnsignedLong(value));
  }

  @Override
  public Object asObject() {
    return ref.isPrimitive() ? value : ref.get();
  }
}
