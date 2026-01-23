package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import java.util.function.Consumer;

public final class CharSequenceTraits implements FormatTraits, Consumer<StringBuilder> {

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
  public RefSlot ref() {
    return RefSlot.of(value);
  }

  @Override
  public void accept(StringBuilder stringBuilder) {
    if (value instanceof String) {
      stringBuilder.append((String) value);
    } else {
      stringBuilder.append(value);
    }
  }

  @Override
  public Seq asSeq() {
    return Seq.lazy(this, value.length());
  }
}
