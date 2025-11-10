package io.fastprintf.number;

/**
 * The base interface for all internal number representations within the fast-printf library.
 *
 * <p>This interface provides a common abstraction for both integer ({@link IntForm}) and
 * floating-point ({@link FloatForm}) types. Its primary responsibility is to offer a unified way to
 * query the sign of a number, which is essential for correctly applying formatting flags like '+'
 * or a leading space.
 *
 * <p>This is an internal API used by the formatting engine and is not intended for direct use by
 * clients of the library.
 *
 * @see IntForm
 * @see FloatForm
 */
public interface NumberForm {

  /**
   * Returns the signum function of this number.
   *
   * @return -1, 0, or 1 as the value of this number is negative, zero, or positive, respectively.
   */
  int signum();

  /**
   * Checks if the number is negative.
   *
   * @return {@code true} if {@link #signum()} is -1, otherwise {@code false}.
   */
  default boolean isNegative() {
    return signum() < 0;
  }

  /**
   * Checks if the number is positive.
   *
   * @return {@code true} if {@link #signum()} is 1, otherwise {@code false}.
   */
  default boolean isPositive() {
    return signum() > 0;
  }

  /**
   * Checks if the number is zero.
   *
   * @return {@code true} if {@link #signum()} is 0, otherwise {@code false}.
   */
  default boolean isZero() {
    return signum() == 0;
  }
}
