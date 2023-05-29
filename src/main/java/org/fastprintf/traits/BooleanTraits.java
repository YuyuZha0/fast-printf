package org.fastprintf.traits;

public final class BooleanTraits extends AbstractTraits {

  private final boolean value;

  public BooleanTraits(boolean value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return false;
  }

  @Override
  public int asInt() {
    return value ? 1 : 0;
  }

  @Override
  public long asLong() {
    return value ? 1 : 0;
  }

  @Override
  public long asUnsignedLong() {
    return value ? 1 : 0;
  }

  @Override
  public double asDouble() {
    return value ? 1D : 0D;
  }

  @Override
  public String asString() {
    return Boolean.toString(value);
  }
}
