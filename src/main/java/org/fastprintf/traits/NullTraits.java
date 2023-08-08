package org.fastprintf.traits;

import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;

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
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatForm asFloatForm() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String asString() {
    return "null";
  }

  @Override
  public int asInt() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object value() {
    return null;
  }
}
