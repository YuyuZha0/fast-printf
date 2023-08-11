package org.fastprintf.number;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FloatFormTest {

  private static void assertDouble(double d, int precision) {
    assertTrue(precision >= 0);
    FloatForm floatFamily = FloatForm.valueOf(d);
    assertDoubleLayout(floatFamily.generalLayout(precision), d, precision, "g");
    assertDoubleLayout(floatFamily.scientificLayout(precision), d, precision, "e");
    assertDoubleLayout(floatFamily.decimalLayout(precision), d, precision, "f");
    // assertDoubleLayout(floatFamily.hexLayout(precision), d, precision, "a");
  }

  private static void assertDoubleLayout(FloatLayout layout, double d, int precision, String spec) {
    String format = "%." + precision + spec;
    String s = String.format(format, Math.abs(d));
    double d1 = Double.parseDouble(layout.toString());
    double d2 = Double.parseDouble(s);
    if (Double.isNaN(d1)) {
      assertTrue(Double.isNaN(d2));
    } else if (Double.isInfinite(d1)) {
      assertTrue(Double.isInfinite(d2));
    } else {
      assertEquals(s + ", " + layout, d2, d1, 0);
    }
  }

  private static void assertBigDecimal(BigDecimal value, int precision) {
    assertTrue(precision >= 0);
    FloatForm floatFamily = FloatForm.valueOf(value);
    assertBigDecimalLayout(floatFamily.generalLayout(precision), value, precision, "g");
    assertBigDecimalLayout(floatFamily.scientificLayout(precision), value, precision, "e");
    assertBigDecimalLayout(floatFamily.decimalLayout(precision), value, precision, "f");
  }

  private static void assertBigDecimalLayout(
      FloatLayout layout, BigDecimal value, int precision, String spec) {
    String format = "%." + precision + spec;
    String s = String.format(format, value.abs());
    BigDecimal d1 = new BigDecimal(layout.toString());
    BigDecimal d2 = new BigDecimal(s);
    assertEquals(s + ", " + layout, 0, d1.compareTo(d2));
  }

  @Test
  public void testDouble() {
    int[] precisions = {1, 3, 5, 8};
    for (int p : precisions) {
      assertDouble(0D, p);
      assertDouble(1D, p);
      assertDouble(-1D, p);
      assertDouble(0.1D, p);
      assertDouble(-0.1D, p);
      assertDouble(Math.PI, p);
      assertDouble(-Math.PI, p);
      assertDouble(Double.MAX_VALUE, p);
      assertDouble(Double.MIN_VALUE, p);
      assertDouble(Math.E * 1000, p);
      assertDouble(-Math.E * 0.001, p);
      assertDouble(Long.MAX_VALUE, p);
      assertDouble(Long.MIN_VALUE, p);
      assertDouble(1D / Integer.MAX_VALUE, p);
      assertDouble(1D / Integer.MIN_VALUE, p);
    }
  }

  @Test
  public void testBigDecimal() {
    int[] precisions = {1, 3, 5, 8, 16};
    for (int p : precisions) {
      assertBigDecimal(BigDecimal.ZERO, p);
      assertBigDecimal(BigDecimal.ONE, p);
      assertBigDecimal(BigDecimal.ONE.negate(), p);
      assertBigDecimal(BigDecimal.valueOf(0.1), p);
      assertBigDecimal(BigDecimal.valueOf(-0.1), p);
      assertBigDecimal(BigDecimal.valueOf(Math.PI), p);
      assertBigDecimal(BigDecimal.valueOf(-Math.PI), p);
      assertBigDecimal(BigDecimal.valueOf(Double.MAX_VALUE), p);
      assertBigDecimal(BigDecimal.valueOf(Double.MIN_VALUE), p);
      assertBigDecimal(BigDecimal.valueOf(Math.E * 1000), p);
      assertBigDecimal(BigDecimal.valueOf(-Math.E * 0.001), p);
      assertBigDecimal(BigDecimal.valueOf(Long.MAX_VALUE), p);
      assertBigDecimal(BigDecimal.valueOf(Long.MIN_VALUE), p);
      assertBigDecimal(
          BigDecimal.ONE.divide(BigDecimal.valueOf(Integer.MAX_VALUE), MathContext.DECIMAL128), p);
      assertBigDecimal(
          BigDecimal.ONE.divide(BigDecimal.valueOf(Integer.MIN_VALUE), MathContext.DECIMAL128), p);
    }
  }
}
