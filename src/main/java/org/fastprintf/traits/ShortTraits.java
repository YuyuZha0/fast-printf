package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class ShortTraits implements FormatTraits {

  private final short value;

  public ShortTraits(short value) {
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
    return Short.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public char asChar() {
    return (char) value;
  }

  @Override
  public Object value() {
    return value;
  }
}
