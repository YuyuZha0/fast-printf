package org.fastprintf.traits;

import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;

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
  public char asChar() {
    if (value.length() >= 1) {
      return value.charAt(0);
    }
    return Character.MIN_VALUE;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(Long.parseLong(value.toString()));
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(Double.parseDouble(value.toString()));
  }

  @Override
  public Object value() {
    return value;
  }
}
