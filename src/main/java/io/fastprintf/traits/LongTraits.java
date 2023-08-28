package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.util.Utils;

import java.time.temporal.TemporalAccessor;

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

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return Utils.longToInstant(value);
  }
}
