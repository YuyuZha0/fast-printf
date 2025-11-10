package io.fastprintf.traits;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import java.time.temporal.TemporalAccessor;

/**
 * A type-specific handler that provides optimized conversions for a single formatting argument.
 *
 * <p>This interface is a central component of the {@code fast-printf} performance model. Instead of
 * performing {@code instanceof} checks or using reflection within the critical formatting loop, the
 * {@link Args} container pre-processes each argument by wrapping it in a concrete {@code
 * FormatTraits} implementation. Each implementation acts as a specialized "strategy" for a single
 * data type (e.g., {@code int}, {@code double}, {@link String}), knowing exactly how to provide the
 * necessary representations for different format specifiers.
 *
 * <p>For example, when the formatter encounters a {@code %d} specifier, it calls {@link
 * #asIntForm()} on the corresponding argument's {@code FormatTraits} handler. Similarly, a {@code
 * %f} specifier triggers a call to {@link #asFloatForm()}. This "ahead-of-time" type dispatch
 * eliminates runtime overhead and ensures high performance.
 *
 * <p>This is an internal-facing interface. Users interact with it indirectly by passing arguments
 * to {@link FastPrintf}, typically via the {@link Args} class.
 *
 * @see Args
 * @see FastPrintf
 * @see IntForm
 * @see FloatForm
 */
public interface FormatTraits {

  /**
   * Indicates whether the underlying argument is {@code null}.
   *
   * @return {@code true} if the original argument was {@code null}, otherwise {@code false}.
   */
  default boolean isNull() {
    return false;
  }

  /**
   * Provides an integer representation of the argument suitable for integral format specifiers
   * (e.g., {@code %d}, {@code %x}, {@code %o}).
   *
   * @return An {@link IntForm} wrapper, which provides methods for rendering the number in various
   *     integer bases.
   * @throws PrintfException if the argument cannot be meaningfully converted to an integer form.
   */
  IntForm asIntForm();

  /**
   * Provides a floating-point representation of the argument suitable for floating-point format
   * specifiers (e.g., {@code %f}, {@code %e}, {@code %g}).
   *
   * @return A {@link FloatForm} wrapper, which provides methods for rendering the number in
   *     different floating-point layouts.
   * @throws PrintfException if the argument cannot be meaningfully converted to a floating-point
   *     form.
   */
  FloatForm asFloatForm();

  /**
   * Provides a string representation of the argument, primarily used by the {@code %s} and {@code
   * %S} specifiers.
   *
   * @return The string value of the argument.
   */
  String asString();

  /**
   * Provides a primitive {@code int} representation of the argument.
   *
   * <p>This method is primarily used to resolve dynamic width and precision arguments (e.g., {@code
   * "%*d"}), but may also be used as a fallback for character conversions ({@code %c}).
   *
   * @return The argument converted to an {@code int}.
   * @throws PrintfException if the argument cannot be converted to an {@code int}.
   */
  int asInt();

  /**
   * Provides a date/time representation of the argument, used by the {@code %t} and {@code %T}
   * specifiers.
   *
   * @return The argument as a {@link TemporalAccessor}.
   * @throws PrintfException if the argument cannot be converted to a temporal type.
   */
  default TemporalAccessor asTemporalAccessor() {
    throw new PrintfException("Cannot convert [%s] to TemporalAccessor", ref());
  }

  /**
   * Provides a primitive {@code char} representation of the argument, used by the {@code %c}
   * specifier.
   *
   * @return The argument as a {@code char}.
   * @throws PrintfException if the argument cannot be converted to a {@code char}.
   */
  default char asChar() {
    return (char) asInt();
  }

  /**
   * Returns a slot containing information about the original argument's identity.
   *
   * <p>This method provides access to a {@link RefSlot}, which encapsulates the original argument
   * object. This allows specifiers like {@code %p} to correctly access the object's identity, while
   * also making a clear distinction for values that originated from primitives and have no stable
   * identity.
   *
   * @return A {@link RefSlot} instance representing the argument's source.
   */
  RefSlot ref();

  /**
   * Provides the most sensible object representation of this trait's value.
   *
   * <p>This method is a flexible way for formatters to retrieve the argument as an {@code Object}.
   * The behavior varies based on the source of the argument:
   *
   * <ul>
   *   <li>For arguments that were originally objects (e.g., {@code String}, {@code BigInteger}, or
   *       a boxed {@code Integer}), this method returns the original object reference, preserving
   *       its identity.
   *   <li>For arguments that were originally primitives (e.g., {@code int}, {@code double}), this
   *       method returns a standard boxed representation (e.g., an {@code Integer}). Note that in
   *       this case, object identity is <b>not</b> preserved.
   * </ul>
   *
   * <p><b>Warning:</b> Because this method may return a newly boxed object, it should
   * <strong>not</strong> be used for operations that rely on stable object identity, such as the
   * {@code %p} formatter. Such formatters should use {@link #ref()} directly to check if the source
   * was a primitive.
   *
   * @return An object representation of the trait's value.
   */
  default Object asObject() {
    // Default implementation for non-primitive traits is correct.
    // Primitive traits MUST override this.
    RefSlot refSlot = ref();
    if (refSlot.isPrimitive()) {
      // This path should ideally not be taken if primitive traits override this method.
      // It serves as a safeguard.
      throw new UnsupportedOperationException(
          "This trait for a primitive value must override asObject().");
    }
    return refSlot.get();
  }
}
