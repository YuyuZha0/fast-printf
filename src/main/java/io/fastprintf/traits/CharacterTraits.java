package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.time.temporal.TemporalAccessor;

public final class CharacterTraits implements FormatTraits {

  private final char value;

  public CharacterTraits(char value) {
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
    return Character.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public char asChar() {
    return value;
  }

  @Override
  public Object value() {
    return value;
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    throw new PrintfException("Cannot convert Character to TemporalAccessor");
  }
}
