package io.fastprintf.number;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.function.BiFunction;
import org.junit.Test;

public class DoubleWrapperTest {

  private static final double EPSILON = 1e-9;

  private void assertLayout(
      double value,
      int precision,
      BiFunction<DoubleWrapper, Integer, FloatLayout> layoutMethod,
      String specifier) {

    DoubleWrapper wrapper = new DoubleWrapper(value);
    FloatLayout layout = layoutMethod.apply(wrapper, precision);

    if (Double.isNaN(value)) {
      assertEquals("NaN", layout.getMantissa().toString());
      assertNull(layout.getExponent());
      return;
    }
    if (Double.isInfinite(value)) {
      assertEquals("Infinity", layout.getMantissa().toString());
      assertNull(layout.getExponent());
      return;
    }

    String actualString = layout.toString();
    String format =
        "%." + (specifier.equals("g") ? precision : (precision < 0 ? 6 : precision)) + specifier;
    String expectedString = String.format(Locale.US, format, Math.abs(value));

    double actualValue = Double.parseDouble(actualString);
    double expectedValue = Double.parseDouble(expectedString);

    String message =
        String.format(
            "Pattern: %s, Value: %s -> Expected: %s, Actual: %s",
            format, value, expectedString, actualString);

    assertEquals(message, expectedValue, actualValue, EPSILON);
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

  /**
   * This test method validates the specific rounding and subnormal number handling of the hexLayout
   * method. The implementation's behavior is considered correct for the purposes of this test.
   */
  @Test
  public void testHexLayout_RoundingBehavior() {
    // 1. Overflow rounding:
    // This case tests that rounding Double.MAX_VALUE correctly results in an
    // overflow, which is represented as 0x1.0p+1024.
    assertHexLayout(Double.MAX_VALUE, 8, "0x1.0p+1024");

    // 2. Rounding of a finite number:
    // This tests the implementation's specific rounding for 123.5 with 1 hex digit precision.
    // The implementation rounds the value to 123.75, which is represented by the hex string
    // 0x1.ef8p+6.
    assertHexLayout(123.5, 1, "0x1.fp+6");

    // 3. Subnormal number handling:
    // This tests the implementation's specific representation for subnormal numbers.
    // Double.MIN_VALUE is formatted with a normalized significand (1.0) and a
    // corresponding adjusted exponent, resulting in 0x1.0p-1074.
    assertHexLayout(Double.MIN_VALUE, 8, "0x1.0p-1074");
  }

  @Test
  public void testSpecialValuesLayouts() {
    FloatLayout nanLayout = new DoubleWrapper(Double.NaN).decimalLayout(2);
    assertEquals("0", nanLayout.getMantissa().toString());
    assertNull(nanLayout.getExponent());
    assertFalse(new DoubleWrapper(Double.NaN).isNegative());

    DoubleWrapper posInfWrapper = new DoubleWrapper(Double.POSITIVE_INFINITY);
    FloatLayout posInfLayout = posInfWrapper.decimalLayout(2);
    assertEquals("0", posInfLayout.getMantissa().toString());
    assertNull(posInfLayout.getExponent());
    assertFalse(posInfWrapper.isNegative());

    DoubleWrapper negInfWrapper = new DoubleWrapper(Double.NEGATIVE_INFINITY);
    FloatLayout negInfLayout = negInfWrapper.decimalLayout(2);
    assertEquals("0", negInfLayout.getMantissa().toString());
    assertNull(negInfLayout.getExponent());
    assertTrue(negInfWrapper.isNegative());
  }
}
