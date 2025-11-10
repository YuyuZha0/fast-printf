package io.fastprintf.number;

import io.fastprintf.appender.SeqFormatter;
import io.fastprintf.traits.FormatTraits;
import java.math.BigInteger;

/**
 * A unified interface for integer-like numeric types used in formatting.
 *
 * <p>This interface provides a strategy for handling all integer-based arguments ({@code byte},
 * {@code short}, {@code int}, {@code long}, {@link BigInteger}) in a uniform way. It allows the
 * formatting logic in {@link SeqFormatter} to request a string representation in a specific base
 * (decimal, hex, octal) without needing to know the underlying concrete type of the number.
 *
 * <p>This abstraction is key to the library's performance, as it avoids type-checking and branching
 * within the performance-critical formatting code for specifiers like {@code %d}, {@code %x}, and
 * {@code %o}.
 *
 * @see NumberForm
 * @see FormatTraits#asIntForm()
 * @see SeqFormatter
 */
public interface IntForm extends NumberForm {

  /**
   * Creates an {@code IntForm} wrapper for a primitive {@code byte} value.
   *
   * @param value the byte value.
   * @return a new {@code IntForm} instance.
   */
  static IntForm valueOf(byte value) {
    return new ByteWrapper(value);
  }

  /**
   * Creates an {@code IntForm} wrapper for a primitive {@code short} value.
   *
   * @param value the short value.
   * @return a new {@code IntForm} instance.
   */
  static IntForm valueOf(short value) {
    return new ShortWrapper(value);
  }

  /**
   * Creates an {@code IntForm} wrapper for a primitive {@code int} value.
   *
   * @param value the int value.
   * @return a new {@code IntForm} instance.
   */
  static IntForm valueOf(int value) {
    return new IntWrapper(value);
  }

  /**
   * Creates an {@code IntForm} wrapper for a primitive {@code long} value.
   *
   * @param value the long value.
   * @return a new {@code IntForm} instance.
   */
  static IntForm valueOf(long value) {
    return new LongWrapper(value);
  }

  /**
   * Creates an {@code IntForm} wrapper for a {@link BigInteger} value.
   *
   * @param value the BigInteger value.
   * @return a new {@code IntForm} instance.
   */
  static IntForm valueOf(BigInteger value) {
    return new BigIntegerWrapper(value);
  }

  /**
   * Returns the absolute value of this number as a decimal string. This is used for the {@code %d}
   * specifier. The sign is handled separately by the formatter using {@link #signum()}.
   *
   * @return a string representation of the absolute value of this number in base 10.
   */
  String toDecimalString();

  /**
   * Returns the number as an unsigned hexadecimal string. This is used for the {@code %x} and
   * {@code %X} specifiers.
   *
   * @return a string representation of the number in base 16.
   */
  String toHexString();

  /**
   * Returns the number as an unsigned octal string. This is used for the {@code %o} specifier.
   *
   * @return a string representation of the number in base 8.
   */
  String toOctalString();

  /**
   * Returns the number as an unsigned decimal string. This is used for the {@code %u} specifier.
   *
   * @return an unsigned string representation of the number in base 10.
   */
  String toUnsignedDecimalString();
}
