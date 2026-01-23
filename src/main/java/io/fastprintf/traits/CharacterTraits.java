package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;

import java.time.temporal.TemporalAccessor;

public final class CharacterTraits implements FormatTraits {

  private final char value;
  private final RefSlot ref;

  public CharacterTraits(char value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static CharacterTraits ofPrimitive(char value) {
    return new CharacterTraits(value, RefSlot.ofPrimitive());
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(value);
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(value);
  }

  @Override
  public String asString() {
    return Character.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public char asChar() {
    return value;
  }

  @Override
  public RefSlot ref() {
    return ref;
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    throw new PrintfException("Cannot convert Character to TemporalAccessor");
  }

  @Override
  public Object asObject() {
    return ref.isPrimitive() ? value : ref.get();
  }

  @Override
  public Seq asSeq() {
    return Seq.ch(value);
  }
}
