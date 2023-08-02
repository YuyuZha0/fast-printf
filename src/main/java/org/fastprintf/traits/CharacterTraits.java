package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public final class CharacterTraits implements FormatTraits {

  private final char value;

  public CharacterTraits(char value) {
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
    return Character.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public Object value() {
    return value;
  }
}
