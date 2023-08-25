package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public final class IntTraits implements FormatTraits {

  private final int value;

  public IntTraits(int value) {
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
    return Integer.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public Object value() {
    return value;
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return Instant.ofEpochSecond(value);
  }
}
