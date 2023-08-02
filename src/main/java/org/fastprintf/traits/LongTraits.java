package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class LongTraits implements FormatTraits {

  private final long value;

  public LongTraits(long value) {
    this.value = value;
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(value);
  }

  @Override
  public FloatFamily asFloatFamily() {
    return FloatFamily.valueOf(value);
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
