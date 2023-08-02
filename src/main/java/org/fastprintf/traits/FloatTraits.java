package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class FloatTraits implements FormatTraits {

  private final float value;

  public FloatTraits(float value) {
    this.value = value;
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(Math.round(value));
  }

  @Override
  public FloatFamily asFloatFamily() {
    return FloatFamily.valueOf(value);
  }

  @Override
  public String asString() {
    return Float.toString(value);
  }

  @Override
  public int asInt() {
    return Math.round(value);
  }

  @Override
  public Object value() {
    return value;
  }
}
