package org.fastprintf.traits;

public final class LongTraits extends AbstractNumericTraits {

  private final long value;

  public LongTraits(long value) {
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
    return value;
  }

  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public int asInt() {
    return (int) value;
  }

  @Override
  public String asCharSequence() {
    return Long.toString(value);
  }
}
