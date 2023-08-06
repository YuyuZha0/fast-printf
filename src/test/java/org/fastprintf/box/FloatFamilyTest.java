package org.fastprintf.box;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FloatFamilyTest {

  private static void assertDouble(double d, int precision) {
    assertTrue(precision >= 0);
    FloatFamily floatFamily = FloatFamily.valueOf(d);
    assertDoubleLayout(floatFamily.generalLayout(precision), d, precision, "g");
    assertDoubleLayout(floatFamily.scientificLayout(precision), d, precision, "e");
    assertDoubleLayout(floatFamily.decimalLayout(precision), d, precision, "f");
    //assertDoubleLayout(floatFamily.hexLayout(precision), d, precision, "a");
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
  @Ignore
  public void test() {
    // System.out.printf("%.3f%n", 2718.2818284590453D);
    double d = 1.7976931348623157E308;
    System.out.println(d - Math.rint(d));
    System.out.println(FloatFamily.valueOf(d).decimalLayout(3));
    System.out.printf("%.3g%n", -0.002718281828459045);
    System.out.println(FloatFamily.valueOf(-0.002718281828459045).generalLayout(3));
  }
}
