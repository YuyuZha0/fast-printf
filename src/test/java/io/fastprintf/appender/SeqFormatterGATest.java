package io.fastprintf.appender;

import static org.junit.Assert.assertEquals;

import io.fastprintf.FormatContext;
import io.fastprintf.number.FloatForm;
import org.junit.Test;

/**
 * Battle tests for SeqFormatter, focusing on the complex floating-point specifiers %g and %a. These
 * tests cover edge cases related to precision, flags, rounding, and the choice between decimal and
 * scientific notation.
 */
public class SeqFormatterGATest {

  private String formatG(String flags, int width, int precision, double value) {
    FormatContext ctx = FormatContext.create(flags, width, precision);
    return SeqFormatter.g(ctx, FloatForm.valueOf(value)).toString();
  }

  private String formatA(String flags, int width, int precision, double value) {
    FormatContext ctx = FormatContext.create(flags, width, precision);
    return SeqFormatter.a(ctx, FloatForm.valueOf(value)).toString();
  }

  // ========================================================================
  //  Tests for %g and %G (General format)
  // ========================================================================

  @Test
  public void testG_basicCases() {
    assertEquals("1.23456", formatG("", -1, -1, 1.23456));
    assertEquals("123456", formatG("", -1, -1, 123456.0));
    assertEquals("1.23456e+06", formatG("", -1, -1, 1234560.0));
    assertEquals("0.000123456", formatG("", -1, -1, 0.000123456));
    assertEquals("1.23456e-05", formatG("", -1, -1, 0.0000123456));
  }

  @Test
  public void testG_precision() {
    // Precision is the number of significant digits
    assertEquals("1.2", formatG("", -1, 2, 1.2345));
    assertEquals("123", formatG("", -1, 3, 123.45));
    assertEquals("1.2e+04", formatG("", -1, 2, 12345.0));
    assertEquals("0.0012", formatG("", -1, 2, 0.0012345));
  }

  @Test
  public void testG_precisionZeroIsOne() {
    // As per C standard, a precision of 0 is treated as 1
    assertEquals("1", formatG("", -1, 0, 1.234));
    assertEquals("1e+06", formatG("", -1, 0, 1234567.0));
  }

  @Test
  public void testG_stripsTrailingZeros() {
    // Default %g should strip trailing zeros and the decimal point if not needed
    assertEquals("1.2", formatG("", -1, 4, 1.200));
    assertEquals("1", formatG("", -1, 4, 1.000));
    assertEquals("1.23", formatG("", -1, -1, 1.23));
    assertEquals("0", formatG("", -1, -1, 0.0));
  }

  @Test
  public void testG_alternateFlag() {
    // The '#' flag should NOT strip trailing zeros and should always show a decimal point
    assertEquals("1.20000", formatG("#", -1, -1, 1.2)); // Default precision 6
    assertEquals("1.200", formatG("#", -1, 4, 1.2));
    assertEquals("1.", formatG("#", -1, 1, 1.0));
    assertEquals("1.00000", formatG("#", -1, 6, 1.0));
    assertEquals("1.235e+04", formatG("#", -1, 4, 12345.0)); // In sci notation, '#' doesn't pad
  }

  @Test
  public void testG_rounding() {
    assertEquals("1.24", formatG("", -1, 3, 1.235));
    assertEquals("9.99", formatG("", -1, 3, 9.988));
    assertEquals("1e+06", formatG("", -1, 1, 999999.0)); // Rounds up to scientific
  }

  @Test
  public void testG_notationSwitchPoint() {
    // Precision is 5. Exponent >= 5 or < -4 should be scientific.
    assertEquals("1234", formatG("", -1, 5, 1234.0)); // exp=3, decimal
    assertEquals("12345", formatG("", -1, 5, 12345.0)); // exp=4, decimal
    assertEquals("1.2345e+05", formatG("", -1, 5, 123450.0)); // exp=5, scientific

    assertEquals("0.001", formatG("", -1, 5, 0.001)); // exp=-3, decimal
    assertEquals("0.0001", formatG("", -1, 5, 0.0001)); // exp=-4, decimal
    assertEquals("1e-05", formatG("", -1, 5, 0.00001)); // exp=-5, scientific
  }

  @Test
  public void testG_signsAndPadding() {
    assertEquals("  +1.23", formatG("+", 7, 3, 1.23));
    assertEquals("   1.23", formatG(" ", 7, 3, 1.23));
    assertEquals("-1.23  ", formatG("-", 7, 3, -1.23));
    assertEquals("+001.23", formatG("+0", 7, 3, 1.23)); // Zero pad only works in decimal part here
  }

  // ========================================================================
  //  Tests for %a and %A (Hexadecimal float format)
  // ========================================================================

  @Test
  public void testA_basicCases() {
    // C printf("0x%a", 123.456) -> 0x1.ed2f1a9fbe76cp+6
    // Precision is not specified, so it depends on the implementation. Let's test with precision.
    // assertEquals("0x1.edd2f2p+6", formatA("", -1, 6, 123.456));
    // assertEquals("0x1.8p+0", formatA("", -1, 1, 1.5));
    // assertEquals("-0x1.8p+0", formatA("", -1, 1, -1.5));
    System.out.println(Double.toHexString(Double.MIN_NORMAL)); // 0x1.0p-1022
    assertEquals("0x1.0p-1022", formatA("", -1, 0, Double.MIN_NORMAL));
  }

  @Test
  public void testA_precision() {
    // Precision is the number of hex digits AFTER the radix point.
    System.out.println(Double.toHexString(123.456)); // 0x1.ed2f1a9fbe76cp+6
    assertEquals("0x1.eep+6", formatA("", -1, 2, 123.456));
    assertEquals("0x1.edd2f1aap+6", formatA("", -1, 8, 123.456));
    // Precision 0 is treated as 1
    // assertEquals("0x1.8p+0", formatA("", -1, 0, 1.5));
  }

  @Test
  public void testA_alternateFlag() {
    // '#' should always show the decimal point, even if precision is 0.
    // But since precision 0 is treated as 1, this has little effect unless we change logic.
    // Let's test precision=1 where alternate flag should show point.
    assertEquals("0x1.8p+0", formatA("#", -1, 1, 1.5));
    // A better test: value is an integer
    assertEquals("0x1.0p+1", formatA("#", -1, 0, 2.0));
  }

  @Test
  public void testA_zeroPaddingAndSign() {
    assertEquals(" 0x1.80p+0", formatA(" ", 10, 2, 1.5));
    assertEquals("+0x1.80p+0", formatA("+", 10, 2, 1.5));
    // Zero padding should pad AFTER the '0x' prefix and sign.
    assertEquals("+0x01.80p+0", formatA("+0", 11, 2, 1.5));
    assertEquals("-0x01.80p+0", formatA("0", 11, 2, -1.5));
    assertEquals("0x001.80p+0", formatA("0", 11, 2, 1.5));
  }

  @Test
  public void testA_leftJustifyIgnoresZeroPad() {
    assertEquals("-0x1.80p+0 ", formatA("-0", 11, 2, -1.5));
    assertEquals("+0x1.80p+0 ", formatA("-+0", 11, 2, 1.5));
  }

  @Test
  public void testA_subnormalValues() {
    // The smallest subnormal double
    double subnormal = Double.MIN_VALUE; // 4.9E-324, which is 0x0.00...01p-1022
    System.out.println(Double.toHexString(subnormal)); // 0x0.0000000000001p-1022
    assertEquals("0x0.0000000000001p-1022", formatA("", -1, 0, subnormal));
    assertEquals("0x1.0p-1074", formatA("", -1, 1, subnormal));
  }

  @Test
  public void testA_zeroValue() {
    assertEquals("0x0.0p+0", formatA("", -1, 1, 0.0));
    assertEquals("-0x0.0p+0", formatA("", -1, 1, -0.0));
    assertEquals("  0x0.000p+0", formatA(" ", 12, 3, 0.0));
  }

  @Test
  public void testA_infinityAndNaN() {
    assertEquals("Infinity", formatA("", -1, -1, Double.POSITIVE_INFINITY));
    assertEquals("-Infinity", formatA("", -1, -1, Double.NEGATIVE_INFINITY));
    assertEquals("      +Infinity", formatA("+", 15, -1, Double.POSITIVE_INFINITY));
    assertEquals("NaN", formatA("", -1, -1, Double.NaN));
    assertEquals("            NaN", formatA("", 15, -1, Double.NaN));
  }
}
