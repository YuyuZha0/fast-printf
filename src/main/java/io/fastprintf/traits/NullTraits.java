package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.time.temporal.TemporalAccessor;

public final class NullTraits implements FormatTraits {

  private static final NullTraits INSTANCE = new NullTraits();

  private NullTraits() {}

  public static NullTraits getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public IntForm asIntForm() {
    throw new PrintfException("null cannot be converted to int");
  }

  @Override
  public FloatForm asFloatForm() {
    throw new PrintfException("null cannot be converted to float");
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    throw new PrintfException("null cannot be converted to TemporalAccessor");
  }

  @Override
  public String asString() {
    return "null";
  }

  @Override
  public int asInt() {
    throw new PrintfException("null cannot be converted to int");
  }

  @Override
  public Object value() {
    return null;
  }
}
