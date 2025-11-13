package io.fastprintf.util.internal;

import static org.junit.Assert.*;

import java.util.Random;
import org.junit.Assume;
import org.junit.Test;

public class DoubleToDecimalTest {

  private static final int JAVA_VERSION = getMajorJavaVersion();

  private static int getMajorJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return -1; // Unable to parse version
    }
  }

  // Helper method to avoid duplicating assertion logic
  private void assertRendersSame(double d) {
    Assume.assumeTrue("This test runs only on JDK 21", JAVA_VERSION >= 21);

    String expected = Double.toString(d);

    // Test DoubleToDecimal.toString()
    String actualToString = DoubleToDecimal.toString(d);
    assertEquals(
        "toString() mismatch for value: "
            + d
            + " (bits: "
            + Long.toHexString(Double.doubleToLongBits(d))
            + ")",
        expected,
        actualToString);

    // Test DoubleToDecimal.appendTo(StringBuilder)
    StringBuilder sb = new StringBuilder();
    try {
      DoubleToDecimal.appendTo(d, sb);
      String actualAppendTo = sb.toString();
      assertEquals(
          "appendTo() mismatch for value: "
              + d
              + " (bits: "
              + Long.toHexString(Double.doubleToLongBits(d))
              + ")",
          expected,
          actualAppendTo);
    } catch (Exception e) {
      fail("appendTo() threw an exception for value: " + d + " - " + e.getMessage());
    }
  }

  @Test
  public void testSpecialValues() {
    assertRendersSame(Double.NaN);
    assertRendersSame(Double.POSITIVE_INFINITY);
    assertRendersSame(Double.NEGATIVE_INFINITY);
    assertRendersSame(0.0);
    assertRendersSame(-0.0);
  }

  @Test
  public void testBoundaryValues() {
    assertRendersSame(Double.MAX_VALUE);
    assertRendersSame(Double.MIN_VALUE);
    assertRendersSame(Double.MIN_NORMAL);

    // Also test against Float boundaries, as they are common inputs
    assertRendersSame(Float.MAX_VALUE);
    assertRendersSame(Float.MIN_VALUE);
    assertRendersSame(Float.MIN_NORMAL);
  }

  @Test
  public void testIntegerBoundaryValues() {
    assertRendersSame(Integer.MAX_VALUE);
    assertRendersSame(Integer.MIN_VALUE);
    assertRendersSame(Long.MAX_VALUE);
    assertRendersSame(Long.MIN_VALUE);
  }

  @Test
  public void testCommonValues() {
    assertRendersSame(1.0);
    assertRendersSame(-1.0);
    assertRendersSame(123.456);
    assertRendersSame(-123.456);
    assertRendersSame(Math.PI);
    assertRendersSame(Math.E);
  }

  @Test
  public void testScientificNotationValues() {
    assertRendersSame(1.23456e10);
    assertRendersSame(-1.23456e-10);
    assertRendersSame(1.0e20);
    assertRendersSame(-1.0e20);
    // Test values that may trigger different formatting paths in the 'g' format
    assertRendersSame(9999999.0);
    assertRendersSame(10000000.0);
    assertRendersSame(0.0001);
    assertRendersSame(0.00009999999999999999);
  }

  @Test
  public void testSubnormalValues() {
    // Smallest positive value
    assertRendersSame(5e-324);
    assertRendersSame(-5e-324);

    // Test the transition from subnormal to normal
    assertRendersSame(Double.MIN_NORMAL);
    assertRendersSame(Math.nextDown(Double.MIN_NORMAL));
  }

  @Test
  public void testSuccessorsAndPredecessors() {
    double[] bases = {1.0, 10.0, 1000.0, 1e10, 1e20, Double.MIN_NORMAL, Double.MAX_VALUE};
    for (double base : bases) {
      assertRendersSame(base);
      assertRendersSame(Math.nextUp(base));
      assertRendersSame(Math.nextDown(base));
    }
  }

  @Test
  public void testRandomValues() {
    // Use a fixed seed for reproducibility
    Random random = new Random(12345L);

    // Test 10,000 random values to catch obscure cases
    for (int i = 0; i < 10000; i++) {
      // Generate doubles from their raw long bits to get a good distribution,
      // including subnormals, infinities, and NaNs.
      long randomBits = random.nextLong();
      double d = Double.longBitsToDouble(randomBits);
      assertRendersSame(d);
    }
  }
}
