package io.fastprintf;

import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;

/**
 * A type-safe and high-performance container for arguments to be formatted by {@link FastPrintf}.
 *
 * <p>The {@code Args} interface is a core component of the {@code fast-printf} library's
 * performance model. Instead of relying on {@code Object...} varargs directly in the formatting
 * path, this container performs upfront type analysis, wrapping each argument in an optimized,
 * type-specific handler (a {@link FormatTraits} implementation). This "ahead-of-time" type dispatch
 * avoids reflection and repeated {@code instanceof} checks inside the performance-critical
 * formatting loop.
 *
 * <p>This interface supports a fluent builder-style API for constructing the argument list, as well
 * as static factory methods for common use cases.
 *
 * <h3>Usage Patterns:</h3>
 *
 * <p><b>1. Static Factory for Varargs (most common):</b>
 *
 * <pre>{@code
 * FastPrintf formatter = FastPrintf.compile("ID: %d, Name: %s");
 * // The of() method is the easiest way to pass arguments.
 * String result = formatter.format(Args.of(123, "test"));
 * // Or more conveniently, using the varargs overload on FastPrintf:
 * String result2 = formatter.format(123, "test");
 * }</pre>
 *
 * <p><b>2. Fluent Builder Pattern:</b>
 *
 * <p>This pattern is useful for building an argument list dynamically or for ensuring primitive
 * types are not boxed.
 *
 * <pre>{@code
 * Args args = Args.create()
 *     .putInt(1024)
 *     .putString("log-message")
 *     .putBoolean(true)
 *     .putDouble(Math.PI);
 * formatter.format(args);
 * }</pre>
 *
 * @see FastPrintf
 * @see FormatTraits
 */
public interface Args extends Iterable<FormatTraits> {

  /**
   * Creates an {@code Args} container from a varargs array of objects.
   *
   * <p>This is the most common factory method for creating an argument list. Each object in the
   * array is inspected and wrapped in the most appropriate {@link FormatTraits} implementation.
   *
   * @param values the arguments to be formatted.
   * @return a new {@code Args} instance containing the provided values.
   */
  static Args of(Object... values) {
    if (values == null) {
      // Treat a null varargs array as a single null argument.
      // This is more robust than throwing an NPE and often matches user intent.
      return new ArgsImpl(1).putNull();
    }
    ArgsImpl args = new ArgsImpl(values.length);
    for (Object value : values) {
      args.put(value);
    }
    return args;
  }

  /**
   * Creates an {@code Args} container from an {@link Iterable} of objects.
   *
   * @param values an {@code Iterable} (such as a {@link List}) containing the arguments.
   * @return a new {@code Args} instance containing the provided values.
   */
  static Args of(Iterable<?> values) {
    Preconditions.checkNotNull(values, "values");
    ArgsImpl args;
    if (values instanceof Collection) {
      args = new ArgsImpl(((Collection<?>) values).size());
    } else {
      args = new ArgsImpl();
    }
    for (Object value : values) {
      args.put(value);
    }
    return args;
  }

  /**
   * Creates a new, empty {@code Args} container, ready to be populated using the fluent {@code put}
   * methods.
   *
   * @return a new, empty {@code Args} instance.
   */
  static Args create() {
    return new ArgsImpl();
  }

  /**
   * Returns an unmodifiable list of the original objects that were added to this container.
   *
   * @return a {@link List} of the original argument values.
   */
  List<Object> values();

  /**
   * Appends a {@code null} argument to the list.
   *
   * @return this {@code Args} instance for method chaining.
   */
  Args putNull();

  /**
   * Appends a primitive {@code boolean} argument to the list. Avoids boxing.
   *
   * @param value the boolean value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putBoolean(boolean value);

  /**
   * Appends a boxed {@code Boolean} argument to the list, handling {@code null}.
   *
   * @param value the {@code Boolean} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putBooleanOrNull(Boolean value);

  /**
   * Appends a primitive {@code char} argument to the list. Avoids boxing.
   *
   * @param value the char value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putChar(char value);

  /**
   * Appends a boxed {@code Character} argument to the list, handling {@code null}.
   *
   * @param value the {@code Character} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putCharOrNull(Character value);

  /**
   * Appends a primitive {@code byte} argument to the list. Avoids boxing.
   *
   * @param value the byte value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putByte(byte value);

  /**
   * Appends a boxed {@code Byte} argument to the list, handling {@code null}.
   *
   * @param value the {@code Byte} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putByteOrNull(Byte value);

  /**
   * Appends a primitive {@code short} argument to the list. Avoids boxing.
   *
   * @param value the short value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putShort(short value);

  /**
   * Appends a boxed {@code Short} argument to the list, handling {@code null}.
   *
   * @param value the {@code Short} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putShortOrNull(Short value);

  /**
   * Appends a primitive {@code int} argument to the list. Avoids boxing.
   *
   * @param value the int value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putInt(int value);

  /**
   * Appends a boxed {@code Integer} argument to the list, handling {@code null}.
   *
   * @param value the {@code Integer} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putIntOrNull(Integer value);

  /**
   * Appends a primitive {@code long} argument to the list. Avoids boxing.
   *
   * @param value the long value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putLong(long value);

  /**
   * Appends a boxed {@code Long} argument to the list, handling {@code null}.
   *
   * @param value the {@code Long} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putLongOrNull(Long value);

  /**
   * Appends a primitive {@code float} argument to the list. Avoids boxing.
   *
   * @param value the float value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putFloat(float value);

  /**
   * Appends a boxed {@code Float} argument to the list, handling {@code null}.
   *
   * @param value the {@code Float} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putFloatOrNull(Float value);

  /**
   * Appends a primitive {@code double} argument to the list. Avoids boxing.
   *
   * @param value the double value to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putDouble(double value);

  /**
   * Appends a boxed {@code Double} argument to the list, handling {@code null}.
   *
   * @param value the {@code Double} value to add, or {@code null}.
   * @return this {@code Args} instance for method chaining.
   */
  Args putDoubleOrNull(Double value);

  /**
   * Appends a date/time object that implements {@link TemporalAccessor}.
   *
   * @param value the {@code TemporalAccessor} to add (e.g., {@code Instant}, {@code
   *     LocalDateTime}).
   * @return this {@code Args} instance for method chaining.
   */
  Args putDateTime(TemporalAccessor value);

  /**
   * Appends a {@link String} argument to the list.
   *
   * @param value the {@code String} value to add.
   * @return this {@code Args} instance for method chaining.
   */
  default Args putString(String value) {
    return putCharSequence(value);
  }

  /**
   * Appends a {@link CharSequence} argument to the list.
   *
   * @param value the {@code CharSequence} to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putCharSequence(CharSequence value);

  /**
   * Appends a {@link BigInteger} argument to the list.
   *
   * @param value the {@code BigInteger} to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putBigInteger(BigInteger value);

  /**
   * Appends a {@link BigDecimal} argument to the list.
   *
   * @param value the {@code BigDecimal} to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args putBigDecimal(BigDecimal value);

  /**
   * Appends a generic {@link Object} argument to the list.
   *
   * <p>This method performs runtime type inspection to dispatch to the most specific {@code put}
   * method (e.g., calling {@code putInt} if the object is an {@code Integer}). This is the
   * general-purpose method used by the static factory methods.
   *
   * @param value the object to add.
   * @return this {@code Args} instance for method chaining.
   */
  Args put(Object value);
}
