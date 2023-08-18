package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

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
    try {
      return Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      throw new PrintfException("Cannot convert \"" + value + "\" to int", e);
    }
  }

  @Override
  public char asChar() {
    if (value.length() >= 1) {
      return value.charAt(0);
    }
    throw new PrintfException("Empty string cannot be converted to char");
  }

  @Override
  public IntForm asIntForm() {
    try {
      return IntForm.valueOf(Long.parseLong(value.toString()));
    } catch (NumberFormatException e) {
      throw new PrintfException("Cannot convert \"" + value + "\" to int", e);
    }
  }

  @Override
  public FloatForm asFloatForm() {
    try {
      return FloatForm.valueOf(Double.parseDouble(value.toString()));
    } catch (NumberFormatException e) {
      throw new PrintfException("Cannot convert \"" + value + "\" to float", e);
    }
  }

  @Override
  public Object value() {
    return value;
  }
}
