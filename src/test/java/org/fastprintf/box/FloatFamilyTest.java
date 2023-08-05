package org.fastprintf.box;

import org.fastprintf.seq.Seq;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FloatFamilyTest {

  private static void assertDouble(double d, int precision) {
    assertTrue(precision >= 0);
    FloatFamily floatFamily = FloatFamily.valueOf(d);
    assertDoubleLayout(floatFamily.generalLayout(precision), d, precision);
    assertDoubleLayout(floatFamily.scientificLayout(precision), d, precision);
    assertDoubleLayout(floatFamily.decimalLayout(precision), d, precision);
    // assertDoubleLayout(floatFamily.hexLayout(precision), d, precision);
  }

  private static int actualPrecision(FloatLayout layout) {
    Seq mantissa = layout.getMantissa();
    int index = mantissa.indexOf('.');
    int length = mantissa.length();
    if (index < 0 || index == length - 1) {
      return 0;
    }
    while (index < length - 1) {
      ++index;
      if (mantissa.charAt(index) != '0') {
        break;
      }
    }
    return length - index;
  }

  private static void assertDoubleLayout(FloatLayout layout, double d, int precision) {
    int actualPrecision = actualPrecision(layout);
    String msg = "d=" + d + ", precision=" + precision + ", layout=" + layout;
    assertTrue(msg, actualPrecision <= precision);
    double d1 = Double.parseDouble(layout.toString());
    assertFalse(Double.isNaN(d1));
    if (Double.isInfinite(d1)) {
      return;
    }
    if (d == 0) {
      assertEquals(msg, 0D, d1, 0D);
    } else {
      double r = Math.abs(d1 / d);
      //System.out.println(layout + ", " + r + ", " + actualPrecision);
      assertEquals(msg, 1D, r, Math.pow(10D, 1 - actualPrecision));
    }
  }

  @Test
  public void testDouble() {
    int[] precisions = {3, 5, 8};
    for (int p : precisions) {
      assertDouble(0D, p);
      assertDouble(1D, p);
      assertDouble(-1D, p);
      assertDouble(0.1D, p);
      assertDouble(-0.1D, p);
      assertDouble(Math.PI, p);
      assertDouble(-Math.PI, p);
      // assertDouble(Double.MAX_VALUE -1, p);
      // assertDouble(Double.MIN_VALUE, p);
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
