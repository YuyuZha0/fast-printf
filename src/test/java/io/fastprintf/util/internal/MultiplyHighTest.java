package io.fastprintf.util.internal;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Test;

/**
 * Tests for the {@link MultiplyHigh} utility. This test suite verifies the correctness of the
 * multiply-high operation against the {@link BigInteger} class, which serves as a reliable oracle.
 * It also explicitly tests the fallback implementation to ensure correctness on JDK 8.
 */
public class MultiplyHighTest {

  /** Calculates the expected high 64 bits of a 128-bit product using BigInteger. */
  private static long calculateExpectedWithBigInteger(long x, long y) {
    BigInteger bx = BigInteger.valueOf(x);
    BigInteger by = BigInteger.valueOf(y);
    BigInteger product = bx.multiply(by);

    // The high 64 bits are obtained by shifting the 128-bit result right by 64.
    return product.shiftRight(64).longValue();
  }

  @Test
  public void testZeroAndOne() {
    assertEquals(0L, MultiplyHigh.multiplyHigh(0L, 12345L));
    assertEquals(0L, MultiplyHigh.multiplyHigh(12345L, 0L));
    assertEquals(0L, MultiplyHigh.multiplyHigh(1L, 12345L));
    assertEquals(0L, MultiplyHigh.multiplyHigh(Long.MAX_VALUE, 1L));
    assertEquals(-1L, MultiplyHigh.multiplyHigh(Long.MAX_VALUE, -1L));
  }

  @Test
  public void testBoundaryValues() {
    // Test combinations of min/max values
    long[][] boundaries = {
      {Long.MAX_VALUE, Long.MAX_VALUE},
      {Long.MIN_VALUE, Long.MIN_VALUE},
      {Long.MAX_VALUE, Long.MIN_VALUE},
      {Long.MAX_VALUE, 2L}, // Test a case that might overflow naively
      {Long.MIN_VALUE, 2L},
      {-1L, -1L}
    };

    for (long[] pair : boundaries) {
      long x = pair[0];
      long y = pair[1];

      long expected = calculateExpectedWithBigInteger(x, y);
      long actual = MultiplyHigh.multiplyHigh(x, y);

      String message = "Mismatch for x=" + x + ", y=" + y;
      assertEquals(message, expected, actual);
    }
  }

  @Test
  public void testRandomValues() {
    // Use a fixed seed for reproducible test runs
    Random random = new Random(42);

    // Run 100,000 trials to get broad coverage
    for (int i = 0; i < 100_000; i++) {
      long x = random.nextLong();
      long y = random.nextLong();

      long expected = calculateExpectedWithBigInteger(x, y);
      long actual = MultiplyHigh.multiplyHigh(x, y);

      String message = "Random mismatch for x=" + x + ", y=" + y;
      assertEquals(message, expected, actual);
    }
  }

  /**
   * This test is crucial. It directly invokes the package-private fallback implementation
   * (`slowMultiplyHigh`) to ensure it is correct. This guarantees that the logic for JDK 8
   * environments is tested, even when the test suite is run on a modern JDK.
   */
  @Test
  public void testSlowMultiplyHighImplementationDirectly() {
    // A smaller set of values is sufficient here, as the random test on the public
    // method is exhaustive. We just need to confirm the fallback logic is sound.
    long[] testValues = {
      0L,
      1L,
      -1L,
      100L,
      -100L,
      Integer.MAX_VALUE,
      Integer.MIN_VALUE,
      Long.MAX_VALUE,
      Long.MIN_VALUE,
      4611686018427387904L, // 2^62
      -4611686018427387904L
    };

    for (long x : testValues) {
      for (long y : testValues) {
        long expected = calculateExpectedWithBigInteger(x, y);
        // Directly call the package-private fallback method
        long actual = MultiplyHigh.slowMultiplyHigh(x, y);

        String message = "Fallback mismatch for x=" + x + ", y=" + y;
        assertEquals(message, expected, actual);
      }
    }
  }
}
