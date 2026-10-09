package io.fastprintf.number;

import static org.junit.Assert.*;

import io.fastprintf.FastPrintf;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;
import java.util.function.BiFunction;
import org.junit.Test;

public class DoubleWrapperTest {

  private void assertLayout(
      double value,
      int precision,
      BiFunction<DoubleWrapper, Integer, FloatLayout> layoutMethod,
      String specifier) {

    DoubleWrapper wrapper = new DoubleWrapper(value);
    FloatLayout layout = layoutMethod.apply(wrapper, precision);

    String actualString = layout.toString();
    String format =
        "%." + (specifier.equals("g") ? precision : (precision < 0 ? 6 : precision)) + specifier;
    String expectedString = String.format(Locale.US, format, Math.abs(value));

    // Layouts omit padding zeros. Compare exact decimal values without rounding back to double.
    BigDecimal actualValue = new BigDecimal(actualString);
    BigDecimal expectedValue = new BigDecimal(expectedString);

    String message =
        String.format(
            "Pattern: %s, Value: %s -> Expected: %s, Actual: %s",
            format, value, expectedString, actualString);

    assertEquals(message, 0, expectedValue.compareTo(actualValue));
    assertEquals("Signum mismatch", Double.compare(value, 0.0), wrapper.signum());
  }

  private void assertHexLayout(double value, int precision, String expectedHexString) {
    DoubleWrapper wrapper = new DoubleWrapper(value);
    FloatLayout layout = wrapper.hexLayout(precision);

    // The FloatLayout does not contain the sign, which is handled externally by the formatter.
    // We replicate that behavior here for the assertion.
    String sign = wrapper.isNegative() ? "-" : "";
    String actualHexString =
        sign + "0x" + layout.getMantissa().toString() + "p" + layout.getExponent().toString();

    assertEquals(
        "Hex string mismatch for " + value + " with precision " + precision,
        expectedHexString,
        actualHexString);
  }

  @Test
  public void testBasics() {
    assertEquals(0, new DoubleWrapper(0.0).signum());
    assertEquals(-1, new DoubleWrapper(-0.0).signum());
    assertEquals(1, new DoubleWrapper(123.45).signum());
    assertEquals(-1, new DoubleWrapper(-123.45).signum());

    assertTrue(new DoubleWrapper(Double.NaN).isNaN());
    assertFalse(new DoubleWrapper(1.0).isNaN());
    assertFalse(new DoubleWrapper(Double.NaN).isNegative());
    assertFalse(new DoubleWrapper(Double.POSITIVE_INFINITY).isNegative());
    assertTrue(new DoubleWrapper(Double.NEGATIVE_INFINITY).isNegative());

    assertTrue(new DoubleWrapper(Double.POSITIVE_INFINITY).isInfinite());
    assertTrue(new DoubleWrapper(Double.NEGATIVE_INFINITY).isInfinite());
    assertFalse(new DoubleWrapper(1.0).isInfinite());

    assertEquals("123.45", new DoubleWrapper(123.45).toString());
  }

  @Test
  public void testDecimalLayout() {
    double[] values = {0.0, -0.0, 1.0, -123.456, Math.PI, Double.MAX_VALUE, Double.MIN_NORMAL};
    int[] precisions = {0, 3, 8};
    for (double value : values) {
      for (int p : precisions) {
        assertLayout(value, p, DoubleWrapper::decimalLayout, "f");
      }
    }
  }

  @Test
  public void testScientificLayout() {
    double[] values = {0.0, -0.0, 1.0, -123.456, Math.PI, Double.MAX_VALUE, Double.MIN_NORMAL};
    int[] precisions = {0, 3, 8};
    for (double value : values) {
      for (int p : precisions) {
        assertLayout(value, p, DoubleWrapper::scientificLayout, "e");
      }
    }
  }

  @Test
  public void testGeneralLayout() {
    double[] values = {
      0.0, -0.0, 1.0, -123.456, 1234567.8, 0.000123, Math.PI, Double.MAX_VALUE, Double.MIN_NORMAL
    };
    int[] precisions = {1, 4, 8};
    for (double value : values) {
      for (int p : precisions) {
        assertLayout(value, p, DoubleWrapper::generalLayout, "g");
      }
    }
  }

  @Test
  public void testHexLayout_SimplePath() {
    // These cases take the simple path in hexLayout (calling Double.toHexString)
    // because precision is 0 or >= 13, and are expected to work correctly.
    assertHexLayout(123.5, 0, "0x1.eep+6");
    assertHexLayout(-456.75, 13, "-0x1.c8cp+8");
    assertHexLayout(0.0, 8, "0x0.0p+0");
    assertHexLayout(-0.0, 8, "-0x0.0p+0");
  }

  @Test
  public void testHexLayout_RoundingBehavior() {
    // 1. Overflow rounding:
    // This case tests that rounding Double.MAX_VALUE correctly results in an
    // overflow, which is represented as 0x1.0p+1024.
    assertHexLayout(Double.MAX_VALUE, 8, "0x1.0p+1024");

    // 2. Rounding of a finite number:
    // 123.5 rounds to 124 with one hexadecimal fractional digit.
    assertHexLayout(123.5, 1, "0x1.fp+6");

    // 3. Subnormal number handling:
    // This tests the implementation's specific representation for subnormal numbers.
    // Double.MIN_VALUE is formatted with a normalized significand (1.0) and a
    // corresponding adjusted exponent, resulting in 0x1.0p-1074.
    assertHexLayout(Double.MIN_VALUE, 8, "0x1.0p-1074");
  }

  @Test
  public void testSubnormalLayoutsExactly() {
    DoubleWrapper smallest = new DoubleWrapper(Double.MIN_VALUE);
    assertEquals("4.9e-324", smallest.scientificLayout(1).toString());
    assertEquals("4.9e-324", smallest.generalLayout(2).toString());
    assertEquals(
        "0." + String.join("", Collections.nCopies(323, "0")) + "49",
        smallest.decimalLayout(325).toString());
    assertEquals("0", smallest.decimalLayout(323).toString());
    assertEquals(
        "2.2250738585072014e-308",
        new DoubleWrapper(Double.MIN_NORMAL).scientificLayout(16).toString());
    assertEquals(
        "2.2250738585072010e-308",
        new DoubleWrapper(Math.nextDown(Double.MIN_NORMAL)).scientificLayout(16).toString());
  }

  @Test
  public void testRoundingBoundariesExactly() {
    assertEquals("1.00e+03", FastPrintf.compile("%.2e").format(999.999));
    assertEquals("1e+03", FastPrintf.compile("%.3g").format(999.999));
    assertEquals("0.0001", FastPrintf.compile("%.3g").format(0.000099999));
    assertHexLayout(1.03125, 1, "0x1.0p+0"); // Halfway: round to even lower digit.
    assertHexLayout(1.09375, 1, "0x1.2p+0"); // Halfway: round to even upper digit.
    assertHexLayout(-Double.MIN_VALUE, 1, "-0x1.0p-1074");
    assertHexLayout(-Double.MAX_VALUE, 1, "-0x1.0p+1024");
  }

  @Test
  public void testFormatterHandlesSpecialValuesBeforeRequestingLayouts() {
    for (char specifier : new char[] {'f', 'e', 'g', 'a', 'F', 'E', 'G', 'A'}) {
      boolean uppercase = Character.isUpperCase(specifier);
      FastPrintf formatter = FastPrintf.compile("%.3" + specifier);
      String infinity = uppercase ? "INFINITY" : "Infinity";
      assertEquals(uppercase ? "NAN" : "NaN", formatter.format(Double.NaN));
      assertEquals(infinity, formatter.format(Double.POSITIVE_INFINITY));
      assertEquals("-" + infinity, formatter.format(Double.NEGATIVE_INFINITY));
    }
    assertEquals("-0.000", FastPrintf.compile("%.3f").format(-0.0));
    assertEquals("-0.000e+00", FastPrintf.compile("%.3e").format(-0.0));
    assertEquals("-0", FastPrintf.compile("%.3g").format(-0.0));
    assertEquals("-0x0.000p+0", FastPrintf.compile("%.3a").format(-0.0));
  }
}
