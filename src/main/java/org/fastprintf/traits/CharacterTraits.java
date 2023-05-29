package org.fastprintf.traits;

public final class CharacterTraits extends AbstractTraits {

  private final char value;

  public CharacterTraits(char value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return false;
  }

  @Override
  public long asLong() {
    return (int) value;
  }

  @Override
  public long asUnsignedLong() {
    return Integer.toUnsignedLong(value);
  }

  @Override
  public double asDouble() {
    return value;
  }


  @Override
  public int asInt() {
    return value;
  }

  @Override
  public String asString() {
    return Character.toString(value);
  }
}
