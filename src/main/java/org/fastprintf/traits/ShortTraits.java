package org.fastprintf.traits;

public final class ShortTraits extends AbstractNumericTraits {

  private final short value;

  public ShortTraits(short value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return value < 0;
  }

  @Override
  public long asLong() {
    return value;
  }

  @Override
  public long asUnsignedLong() {
    return Short.toUnsignedLong(value);
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
  public String asCharSequence() {
    return Short.toString(value);
  }
}
