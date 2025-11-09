package io.fastprintf;

import java.io.IOException;

/**
 * A high-performance, `glibc`-compliant `printf`-style formatter.
 *
 * <p>This interface provides a fast and efficient way to format strings, closely following the
 * behavior of the standard C `printf` function. It is designed as a high-performance replacement
 * for {@link String#format(String, Object...)} in scenarios where `glibc` compatibility and raw
 * speed are critical, such as logging, network protocols, or data serialization.
 *
 * <h3>Core Features:</h3>
 *
 * <ul>
 *   <li><b>Ahead-of-Time Compilation</b>: Format strings are parsed once using {@link
 *       #compile(String)} and reused, eliminating the overhead of parsing on every call.
 *   <li><b>Type Safety and Performance</b>: Arguments are processed through the {@link Args} class,
 *       which uses a highly optimized, non-reflective system for type handling.
 *   <li><b>glibc Compatibility</b>: Adheres to the `glibc` `printf` specification for format
 *       specifiers (e.g., `%S` for uppercase string), flags, width, and precision, rather than
 *       Java's {@link java.util.Formatter}.
 *   <li><b>Zero-Copy Appending</b>: Efficiently constructs the final string without intermediate
 *       array copying, minimizing memory allocation and GC pressure.
 *   <li><b>No Locale Overhead</b>: Formatting is locale-agnostic for maximum performance.
 * </ul>
 *
 * <h3>Basic Usage:</h3>
 *
 * <pre>{@code
 * // 1. Compile the format string once and reuse the formatter.
 * FastPrintf formatter = FastPrintf.compile("User: %s, ID: %#08X, Score: %.2f");
 *
 * // 2. Format arguments.
 * String result = formatter.format("test-user", 255, 98.6);
 *
 * // result will be "User: test-user, ID: 0X0000FF, Score: 98.60"
 * }</pre>
 *
 * @see Args
 * @see <a href="https://cplusplus.com/reference/cstdio/printf/">C++ printf reference</a>
 */
public interface FastPrintf {

  /**
   * Compiles a `printf`-style format string into a reusable {@code FastPrintf} instance.
   *
   * <p>This method performs the expensive work of parsing the format string ahead of time. The
   * returned instance is immutable and thread-safe, and should be stored and reused for best
   * performance.
   *
   * @param format the `printf`-style format string to compile.
   * @return a new, thread-safe {@code FastPrintf} instance.
   * @throws PrintfSyntaxException if the format string contains a syntax error.
   */
  static FastPrintf compile(String format) {
    return FastPrintfImpl.compile(format);
  }

  /**
   * Formats the given arguments and appends the result to the provided {@link Appendable}.
   *
   * <p>This is the most general and efficient formatting method, suitable for writing to a {@link
   * StringBuilder}, {@link java.io.Writer}, or any other {@code Appendable} implementation.
   *
   * @param <T> the type of the {@code Appendable}.
   * @param builder the {@code Appendable} to which the formatted string will be appended.
   * @param args the arguments to be formatted, wrapped in an {@link Args} container.
   * @return the same {@code builder} instance that was passed in.
   * @throws RuntimeException if an {@link IOException} occurs while appending.
   */
  <T extends Appendable> T format(T builder, Args args);

  /**
   * Formats the given arguments and returns the result as a new {@link String}.
   *
   * <p>This is a convenience method that creates a new {@link StringBuilder} internally. For
   * performance-critical code that writes to an existing buffer, prefer {@link #format(Appendable,
   * Args)}.
   *
   * @param args the arguments to be formatted, wrapped in an {@link Args} container.
   * @return the formatted result as a new {@code String}.
   */
  default String format(Args args) {
    return format(new StringBuilder(), args).toString();
  }

  /**
   * Formats the given varargs arguments and returns the result as a new {@link String}.
   *
   * <p>This is a convenience method that wraps the varargs array in an {@link Args} object before
   * formatting.
   *
   * @param values the arguments to be formatted.
   * @return the formatted result as a new {@code String}.
   * @see Args#of(Object...)
   */
  default String format(Object... values) {
    return format(Args.of(values));
  }

  /**
   * Returns a new {@code FastPrintf} instance that uses a {@link ThreadLocal} cache for its
   * internal {@link StringBuilder}.
   *
   * <p>This method may improve performance in highly concurrent, thread-pool-based applications by
   * reducing garbage collection pressure from creating new {@code StringBuilder} objects for each
   * format operation. However, in many modern JVMs, the cost of {@code ThreadLocal} access and
   * buffer clearing can be higher than the cost of allocating a new short-lived object.
   *
   * <p><b>Benchmarking is strongly recommended</b> to determine if this provides a net benefit for
   * a specific workload. If this instance already has the cache enabled, it will return itself.
   *
   * @return a new {@code FastPrintf} instance with {@link ThreadLocal} caching enabled, or this
   *     instance if caching is already enabled.
   */
  FastPrintf enableThreadLocalCache();

  /**
   * Returns a new {@code FastPrintf} instance configured with a specific initial capacity for its
   * internal {@link StringBuilder}.
   *
   * <p>By default, this library uses a heuristic (typically 1.5 times the format string's length)
   * to pre-size the {@code StringBuilder} used for formatting. If you have a specific workload
   * where you know the typical output size, providing an accurate capacity can prevent the {@code
   * StringBuilder} from resizing, offering a minor performance improvement in highly sensitive
   * applications.
   *
   * <p>This method returns a new, immutable instance with the specified capacity. The original
   * instance is not modified. If this instance already has the same capacity, it will return
   * itself.
   *
   * <pre>{@code
   * // If you know your log lines are usually around 256 characters:
   * FastPrintf baseFormatter = FastPrintf.compile("User: %s, Action: %s, Details: %s");
   * FastPrintf optimizedFormatter = baseFormatter.setStringBuilderInitialCapacity(256);
   *
   * // Use the optimized formatter for subsequent calls.
   * optimizedFormatter.format("test-user", "login", "success");
   * }</pre>
   *
   * @param capacity the positive initial capacity for the {@code StringBuilder}.
   * @return a new {@code FastPrintf} instance with the specified capacity, or this instance if the
   *     capacity is unchanged.
   * @throws IllegalArgumentException if the capacity is not positive.
   * @see #enableThreadLocalCache()
   */
  FastPrintf setStringBuilderInitialCapacity(int capacity);
}
