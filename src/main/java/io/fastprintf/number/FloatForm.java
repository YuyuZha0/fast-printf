package io.fastprintf.number;

import io.fastprintf.appender.SeqFormatter;
import io.fastprintf.traits.FormatTraits;
import java.math.BigDecimal;

/**
 * A unified interface for floating-point-like numeric types used in formatting.
 *
 * <p>This interface provides a strategy for handling all floating-point arguments ({@code float},
 * {@code double}, {@link BigDecimal}) in a uniform way. It allows the formatting logic in {@link
 * SeqFormatter} to request a specific "layout" (decimal, scientific, etc.) without needing to know
 * the underlying concrete type of the number.
 *
 * <p>Each layout method returns a {@link FloatLayout} object, which contains the decomposed
 * mantissa and exponent parts ready for rendering. This abstraction is key to the library's
 * performance for specifiers like {@code %f}, {@code %e}, {@code %g}, and {@code %a}.
 *
 * @see NumberForm
 * @see FloatLayout
 * @see FormatTraits#asFloatForm()
 * @see SeqFormatter
 */
public interface FloatForm extends NumberForm {

  /**
   * Creates a {@code FloatForm} wrapper for a primitive {@code double} value.
   *
   * @param value the double value.
   * @return a new {@code FloatForm} instance.
   */
  static FloatForm valueOf(double value) {
    return new DoubleWrapper(value);
  }

  /**
   * Creates a {@code FloatForm} wrapper for a {@link BigDecimal} value.
   *
   * @param value the BigDecimal value.
   * @return a new {@code FloatForm} instance.
   */
  static FloatForm valueOf(BigDecimal value) {
    return new BigDecimalWrapper(value);
  }

  /**
   * Checks if the number is Not-a-Number (NaN).
   *
   * @return {@code true} if the value is NaN, otherwise {@code false}.
   */
  boolean isNaN();

  /**
   * Checks if the number is infinite.
   *
   * @return {@code true} if the value is positive or negative infinity, otherwise {@code false}.
   */
  boolean isInfinite();

  /**
   * Generates the components for formatting with the {@code %g} or {@code %G} (general) specifier.
   * This format chooses between decimal or scientific notation based on the value's magnitude.
   *
   * @param precision the number of significant digits.
   * @return a {@link FloatLayout} containing the mantissa and optional exponent parts.
   */
  FloatLayout generalLayout(int precision);

  /**
   * Generates the components for formatting with the {@code %e} or {@code %E} (scientific)
   * specifier.
   *
   * @param precision the number of digits to appear after the decimal point.
   * @return a {@link FloatLayout} containing the mantissa and exponent parts.
   */
  FloatLayout scientificLayout(int precision);

  /**
   * Generates the components for formatting with the {@code %f} or {@code %F} (decimal) specifier.
   *
   * @param precision the number of digits to appear after the decimal point.
   * @return a {@link FloatLayout} containing the mantissa part (exponent will be null).
   */
  FloatLayout decimalLayout(int precision);

  /**
   * Generates the components for formatting with the {@code %a} or {@code %A} (hexadecimal
   * floating-point) specifier.
   *
   * @param precision the number of hexadecimal digits to appear after the radix point.
   * @return a {@link FloatLayout} containing the hexadecimal mantissa and binary exponent parts.
   */
  FloatLayout hexLayout(int precision);
}
