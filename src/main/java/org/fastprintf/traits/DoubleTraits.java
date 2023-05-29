package org.fastprintf.traits;

public final class DoubleTraits extends AbstractTraits {

  private final double value;

  public DoubleTraits(double value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return Double.compare(value, 0.0) < 0;
  }

  @Override
  public long asLong() {
    return Math.round(value);
  }

  @Override
  public long asUnsignedLong() {
    return Math.round(value);
  }

  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public int asInt() {
    return (int) Math.round(value);
  }

  @Override
  public String asString() {
    return Double.toString(value);
  }
}
