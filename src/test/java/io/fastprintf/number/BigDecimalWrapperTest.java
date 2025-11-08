package io.fastprintf.number;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.function.BiFunction;
import org.junit.Test;

public class BigDecimalWrapperTest {

  private void assertLayout(
      BigDecimal value,
      int precision,
      BiFunction<BigDecimalWrapper, Integer, FloatLayout> layoutMethod,
      String specifier) {

    BigDecimalWrapper wrapper = new BigDecimalWrapper(value);
    // The layout methods operate on the absolute value, sign is handled separately.
    FloatLayout layout = layoutMethod.apply(wrapper, precision);
    String actualString = layout.toString();

    // Use Java's String.format as the ground truth, but apply it to the absolute value
    // to match the wrapper's internal logic.
    String format =
        "%." + (specifier.equals("g") ? precision : (precision < 0 ? 6 : precision)) + specifier;
    String expectedString = String.format(Locale.US, format, value.abs());

    // Compare the numeric values of the absolute representations.
    BigDecimal actualValue = new BigDecimal(actualString);
    BigDecimal expectedValue = new BigDecimal(expectedString);

    String message =
        String.format(
            "Pattern: %s, Value: %s -> Expected: %s, Actual: %s",
            format, value, expectedString, actualString);

    assertEquals(message, 0, expectedValue.compareTo(actualValue));

    // Also, explicitly verify that the signum is stored correctly.
    assertEquals("Signum mismatch", value.signum(), wrapper.signum());
  }

  @Test
  public void testConstructorsAndSignum() {
    assertEquals(0, new BigDecimalWrapper(BigDecimal.ZERO).signum());
    assertEquals(1, new BigDecimalWrapper(BigDecimal.ONE).signum());
    assertEquals(-1, new BigDecimalWrapper(BigDecimal.ONE.negate()).signum());

    assertEquals(0, new BigDecimalWrapper(BigInteger.ZERO, 0).signum());
    assertEquals(1, new BigDecimalWrapper(BigInteger.TEN, 0).signum());
    assertEquals(-1, new BigDecimalWrapper(BigInteger.TEN.negate(), 0).signum());
  }

  @Test
  public void testToString() {
    assertEquals("123.45", new BigDecimalWrapper(new BigDecimal("123.45")).toString());
    assertEquals("-123.45", new BigDecimalWrapper(new BigDecimal("-123.45")).toString());
    assertEquals("0", new BigDecimalWrapper(BigDecimal.ZERO).toString());
  }

  @Test
  public void testIsNaNAndIsInfinite() {
    BigDecimalWrapper wrapper = new BigDecimalWrapper(BigDecimal.TEN);
    assertFalse(wrapper.isNaN());
    assertFalse(wrapper.isInfinite());
  }

  @Test(expected = PrintfException.class)
  public void testHexLayoutThrowsException() {
    BigDecimalWrapper wrapper = new BigDecimalWrapper(BigDecimal.TEN);
    wrapper.hexLayout(10);
  }

  @Test
  public void testDecimalLayout() {
    BigDecimal[] values = {
      new BigDecimal("123.4567"),
      new BigDecimal("-123.4567"),
      BigDecimal.ZERO,
      new BigDecimal("0.000123"),
      new BigDecimal("12345678901234567890.123")
    };
    int[] precisions = {0, 2, 6, 10};

    for (BigDecimal value : values) {
      for (int p : precisions) {
        assertLayout(value, p, BigDecimalWrapper::decimalLayout, "f");
      }
    }
  }

  @Test
  public void testScientificLayout() {
    BigDecimal[] values = {
      new BigDecimal("123.4567"),
      new BigDecimal("-123.4567"),
      BigDecimal.ZERO,
      new BigDecimal("0.000123"),
      new BigDecimal("12345678901234567890.123")
    };
    int[] precisions = {0, 2, 6, 10};

    for (BigDecimal value : values) {
      for (int p : precisions) {
        assertLayout(value, p, BigDecimalWrapper::scientificLayout, "e");
      }
    }
  }

  @Test
  public void testGeneralLayout() {
    BigDecimal[] values = {
      new BigDecimal("123.4567"),
      new BigDecimal("-123.4567"),
      BigDecimal.ZERO,
      new BigDecimal("0.000123"),
      new BigDecimal("0.12345"),
      new BigDecimal("1234567.89"),
      new BigDecimal("12345678901234567890.123")
    };
    // For %g, precision is the number of significant digits.
    int[] precisions = {1, 3, 6, 10};

    for (BigDecimal value : values) {
      for (int p : precisions) {
        assertLayout(value, p, BigDecimalWrapper::generalLayout, "g");
      }
    }
  }

  @Test
  public void testDecimalLayoutRounding() {
    // Test rounding up
    BigDecimal val1 = new BigDecimal("1.235");
    FloatLayout layout1 = new BigDecimalWrapper(val1).decimalLayout(2);
    assertEquals("1.24", layout1.getMantissa().toString());

    // Test rounding down
    BigDecimal val2 = new BigDecimal("1.234");
    FloatLayout layout2 = new BigDecimalWrapper(val2).decimalLayout(2);
    assertEquals("1.23", layout2.getMantissa().toString());

    // Test rounding a zero-prefix number
    BigDecimal val3 = new BigDecimal("0.001235");
    FloatLayout layout3 = new BigDecimalWrapper(val3).decimalLayout(5);
    assertEquals("0.00124", layout3.getMantissa().toString());
  }
}
