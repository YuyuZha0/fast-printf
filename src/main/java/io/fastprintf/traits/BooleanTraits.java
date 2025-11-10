package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

public final class BooleanTraits implements FormatTraits {

  private static final BooleanTraits TRUE = new BooleanTraits(true, RefSlot.ofPrimitive());
  private static final BooleanTraits FALSE = new BooleanTraits(false, RefSlot.ofPrimitive());

  private final boolean value;
  private final RefSlot ref;

  public BooleanTraits(boolean value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static BooleanTraits ofPrimitive(boolean value) {
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
  public RefSlot ref() {
    return ref;
  }

  @Override
  public Object asObject() {
    return ref.isPrimitive() ? value : ref.get();
  }
}
