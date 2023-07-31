package org.fastprintf.box;

import java.math.BigInteger;

public interface IntFamily extends NumberFamily {

  static IntFamily valueOf(byte value) {
    return new ByteBox(value);
  }

  static IntFamily valueOf(short value) {
    return new ShortBox(value);
  }

  static IntFamily valueOf(int value) {
    return new IntBox(value);
  }

  static IntFamily valueOf(long value) {
    return new LongBox(value);
  }

  static IntFamily valueOf(BigInteger value) {
    return new BigIntegerBox(value);
  }

  /**
   * Format this object as a decimal string, without a sign.
   *
   * @return string representation of the absolute value of this object
   */
  String toDecimalString();

  String toHexString();

  String toOctalString();

  String toUnsignedDecimalString();
}
