package io.fastprintf.number;

import java.math.BigInteger;

public interface IntForm extends NumberForm {

  static IntForm valueOf(byte value) {
    return new ByteWrapper(value);
  }

  static IntForm valueOf(short value) {
    return new ShortWrapper(value);
  }

  static IntForm valueOf(int value) {
    return new IntWrapper(value);
  }

  static IntForm valueOf(long value) {
    return new LongWrapper(value);
  }

  static IntForm valueOf(BigInteger value) {
    return new BigIntegerWrapper(value);
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
