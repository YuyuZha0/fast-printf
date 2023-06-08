package org.fastprintf.traits;

public final class FloatTraits extends AbstractNumericTraits {

  private final float value;

  public FloatTraits(float value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return Float.compare(value, 0.0f) < 0;
  }

  @Override
  public String asCharSequence() {
    return Float.toString(value);
  }

  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public int asInt() {
    return Math.round(value);
  }

  @Override
  public long asLong() {
    return Math.round(value);
  }

  @Override
  public long asUnsignedLong() {
    return Integer.toUnsignedLong(Math.round(value));
  }
}
