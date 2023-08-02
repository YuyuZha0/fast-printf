package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class BooleanTraits implements FormatTraits {

  private final boolean value;

  public BooleanTraits(boolean value) {
    this.value = value;
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(asInt());
  }

  @Override
  public FloatFamily asFloatFamily() {
    return FloatFamily.valueOf(asInt());
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
  public Object value() {
    return value;
  }
}
