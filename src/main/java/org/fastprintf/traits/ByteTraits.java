package org.fastprintf.traits;

public final class ByteTraits extends AbstractTraits {

  private final byte value;

  public ByteTraits(byte value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return value < 0;
  }

  @Override
  public String asString() {
    return Byte.toString(value);
  }

  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public long asLong() {
    return value;
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public long asUnsignedLong() {
    return Byte.toUnsignedLong(value);
  }
}
