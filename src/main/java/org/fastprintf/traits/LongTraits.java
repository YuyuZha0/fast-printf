package org.fastprintf.traits;

import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;

public final class LongTraits implements FormatTraits {

  private final long value;

  public LongTraits(long value) {
    this.value = value;
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
    return Long.toString(value);
  }

  @Override
  public int asInt() {
    return (int) value;
  }

  @Override
  public Object value() {
    return value;
  }
}
