package io.fastprintf.util.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A utility class for calculating the high 64 bits of a 128-bit product of two 64-bit integers.
 *
 * <p>This class serves as a polyfill for the {@code Math.multiplyHigh(long, long)} method, which
 * was introduced in JDK 9. This operation is a common requirement in high-precision arithmetic
 * algorithms, such as the "Schubfach" double-to-string conversion method backported for this
 * library. Standard Java multiplication ({@code *}) on two {@code long}s only returns the low 64
 * bits of the full 128-bit result, discarding the upper bits. This utility provides a way to access
 * those upper bits.
 *
 * <h2>Implementation Strategy</h2>
 *
 * <p>To ensure maximum performance, this class dynamically selects the best available
 * implementation at runtime during static initialization:
 *
 * <ol>
 *   <li><b>On JDK 9 or later:</b> It uses a {@link MethodHandle} to invoke the native, highly
 *       optimized {@code Math.multiplyHigh()} intrinsic. This is the fastest path.
 *   <li><b>On JDK 8 or older:</b> When the modern method is not found, it falls back to a pure Java
 *       implementation based on the algorithm described in Henry S. Warren, Jr.'s book, "Hacker's
 *       Delight". This fallback ensures compatibility while still being reasonably performant.
 * </ol>
 *
 * <p>This is an internal utility class and is not intended for public use.
 */
final class MultiplyHigh {

  /**
   * Holds the selected implementation (either the fast MethodHandle-based one or the pure Java
   * fallback) to avoid the lookup cost on every call.
   */
  private static final MultiplyHighImpl IMPL = findImpl();

  private MultiplyHigh() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Returns as a {@code long} the most significant 64 bits of the 128-bit product of two 64-bit
   * factors. This is in contrast to the standard {@code x * y} multiplication, which returns the
   * least significant 64 bits.
   *
   * @param x the first 64-bit factor.
   * @param y the second 64-bit factor.
   * @return the high 64 bits of the 128-bit product {@code x * y}.
   */
  static long multiplyHigh(long x, long y) {
    return IMPL.multiplyHigh(x, y);
  }

  /**
   * A pure Java fallback implementation for {@code multiplyHigh}, used on JDK 8.
   *
   * <p>The technique is adapted from section 8-2 of Henry S. Warren, Jr.'s "Hacker's Delight" (2nd
   * ed.). It simulates 128-bit multiplication by breaking the 64-bit inputs into 32-bit high and
   * low words and combining the partial products.
   *
   * @param x the first value
   * @param y the second value
   * @return the high 64 bits of the product.
   */
  private static long slowMultiplyHigh(long x, long y) {
    // Use technique from section 8-2 of Henry S. Warren, Jr.,
    // Hacker's Delight (2nd ed.) (Addison Wesley, 2013), 173-174.
    long x1 = x >> 32;
    long x2 = x & 0xFFFFFFFFL;
    long y1 = y >> 32;
    long y2 = y & 0xFFFFFFFFL;

    long z2 = x2 * y2;
    long t = x1 * y2 + (z2 >>> 32);
    long z1 = t & 0xFFFFFFFFL;
    long z0 = t >> 32;
    z1 += x2 * y1;

    return x1 * y1 + z0 + (z1 >> 32);
  }

  /**
   * Selects the optimal implementation for {@code multiplyHigh} during class initialization.
   *
   * <p>It attempts to look up {@code java.lang.Math.multiplyHigh} using method handles. If this
   * succeeds (on JDK 9+), it returns a wrapper around that method. If it fails (on JDK 8), it
   * returns a reference to the {@link #slowMultiplyHigh} method.
   *
   * @return The best available implementation of the multiply-high operation.
   */
  private static MultiplyHighImpl findImpl() {
    try {
      MethodHandle methodHandle =
          MethodHandles.publicLookup()
              .findStatic(
                  Math.class,
                  "multiplyHigh",
                  MethodType.methodType(long.class, long.class, long.class));
      return (x, y) -> {
        try {
          return (long) methodHandle.invokeExact(x, y);
        } catch (Throwable t) {
          // This should ideally not happen if the lookup succeeds, but wrap in a
          // RuntimeException as a safeguard.
          throw new RuntimeException("Failed to invoke Math.multiplyHigh", t);
        }
      };
    } catch (NoSuchMethodException | IllegalAccessException e) {
      // Fallback to the pure Java implementation on older JDKs.
      return MultiplyHigh::slowMultiplyHigh;
    }
  }

  /**
   * A functional interface defining the contract for a multiply-high implementation. This allows
   * for a clean strategy pattern where {@code IMPL} can hold either the fast native version or the
   * slow fallback version.
   */
  @FunctionalInterface
  interface MultiplyHighImpl {
    long multiplyHigh(long x, long y);
  }
}
