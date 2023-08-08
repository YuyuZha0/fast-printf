package org.fastprintf.number;

public final class ByteWrapper implements IntForm {

  private final byte value;

  public ByteWrapper(byte value) {
    this.value = value;
  }

  @Override
  public int signum() {
    return Integer.signum(value);
  }

  @Override
  public String toDecimalString() {
    return Integer.toString(Math.abs(value));
  }

  @Override
  public String toHexString() {
    return Integer.toHexString(Byte.toUnsignedInt(value));
  }

  @Override
  public String toOctalString() {
    return Integer.toOctalString(Byte.toUnsignedInt(value));
  }

  @Override
  public String toUnsignedDecimalString() {
    return Integer.toUnsignedString(Byte.toUnsignedInt(value));
  }

  @Override
  public String toString() {
    return Byte.toString(value);
  }
}
