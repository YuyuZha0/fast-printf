package org.fastprintf.traits;

public final class IntTraits extends AbstractNumericTraits implements FormatTraits {

  private final int value;

  public IntTraits(int value) {
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
  public String asCharSequence() {
    return Integer.toString(value);
  }
}
