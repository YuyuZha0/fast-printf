package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class CharSequenceTraits implements FormatTraits {

  private final CharSequence value;

  public CharSequenceTraits(CharSequence value) {
    this.value = value;
  }

  @Override
  public String asString() {
    return value.toString();
  }

  @Override
  public int asInt() {
    return Integer.parseInt(value.toString());
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(Long.parseLong(value.toString()));
  }

  @Override
  public FloatFamily asFloatFamily() {
    return FloatFamily.valueOf(Double.parseDouble(value.toString()));
  }

  @Override
  public Object value() {
    return value;
  }
}
