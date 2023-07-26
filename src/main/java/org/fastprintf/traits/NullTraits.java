package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class NullTraits implements FormatTraits {

  private static final NullTraits INSTANCE = new NullTraits();

  public static NullTraits getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public IntFamily asIntFamily() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatFamily asFloatFamily() {
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
}
