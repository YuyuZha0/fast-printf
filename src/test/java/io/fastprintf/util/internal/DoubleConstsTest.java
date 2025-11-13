package io.fastprintf.util.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

public class DoubleConstsTest {

  @Test
  public void testConstants_areConsistentWithJavaDouble() {
    // Verify that our backported constants match the modern java.lang.Double constants,
    // which they are intended to mirror.
    assertEquals(Double.SIZE, 64); // Pre-condition for the test
    assertEquals(53, DoubleConsts.SIGNIFICAND_WIDTH);
    assertEquals(1023, DoubleConsts.EXP_BIAS);

    // IEEE 754 double-precision format details
    int exponentBits = 11;
    int significandBits = 52; // Explicit bits

    assertEquals(Double.SIZE - significandBits, exponentBits + 1); // +1 for sign bit
    assertEquals(1L << (Double.SIZE - 1), DoubleConsts.SIGN_BIT_MASK);

    // Build the exponent mask from scratch to verify
    long expectedExpMask = ((1L << exponentBits) - 1) << significandBits;
    assertEquals(expectedExpMask, DoubleConsts.EXP_BIT_MASK);

    // Build the significand mask from scratch to verify
    long expectedSignifMask = (1L << significandBits) - 1;
    assertEquals(expectedSignifMask, DoubleConsts.SIGNIF_BIT_MASK);
  }

  @Test
  public void testBitMasks_areMutuallyExclusiveAndCoverAllBits() {
    // The masks should not overlap
    assertEquals(0L, DoubleConsts.SIGN_BIT_MASK & DoubleConsts.EXP_BIT_MASK);
    assertEquals(0L, DoubleConsts.SIGN_BIT_MASK & DoubleConsts.SIGNIF_BIT_MASK);
    assertEquals(0L, DoubleConsts.EXP_BIT_MASK & DoubleConsts.SIGNIF_BIT_MASK);

    // Combining the masks should cover all 64 bits (resulting in -1L or 0xFFF...FFL)
    long allBits =
        DoubleConsts.SIGN_BIT_MASK | DoubleConsts.EXP_BIT_MASK | DoubleConsts.SIGNIF_BIT_MASK;
    assertEquals(-1L, allBits);

    // Verify the magnitude mask
    assertEquals(
        DoubleConsts.EXP_BIT_MASK | DoubleConsts.SIGNIF_BIT_MASK, DoubleConsts.MAG_BIT_MASK);
  }

  @Test
  public void testPrivateConstructor_forCodeCoverage() throws Exception {
    // This test is purely for achieving 100% code coverage by invoking the private
    // constructor, which is designed to prevent instantiation.
    Constructor<DoubleConsts> constructor = DoubleConsts.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected an exception from the private constructor");
    } catch (InvocationTargetException e) {
      // We expect the constructor to throw an exception.
      // Check that the cause is what we expect (e.g., IllegalStateException).
      assertTrue(
          e.getCause() instanceof AssertionError || e.getCause() instanceof IllegalStateException);
    }
  }
}
