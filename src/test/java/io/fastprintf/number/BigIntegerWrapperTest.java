package io.fastprintf.number;

import static org.junit.Assert.assertEquals;

import io.fastprintf.PrintfException;
import java.math.BigInteger;
import org.junit.Test;

public class BigIntegerWrapperTest {

  @Test
  public void testConstructorAndSignum() {
    BigIntegerWrapper zeroWrapper = new BigIntegerWrapper(BigInteger.ZERO);
    assertEquals("Signum of zero should be 0", 0, zeroWrapper.signum());

    BigIntegerWrapper positiveWrapper = new BigIntegerWrapper(BigInteger.TEN);
    assertEquals("Signum of a positive value should be 1", 1, positiveWrapper.signum());

    BigIntegerWrapper negativeWrapper = new BigIntegerWrapper(BigInteger.TEN.negate());
    assertEquals("Signum of a negative value should be -1", -1, negativeWrapper.signum());
  }

  @Test
  public void testToDecimalString() {
    BigInteger value = new BigInteger("12345678901234567890");
    BigIntegerWrapper positiveWrapper = new BigIntegerWrapper(value);
    assertEquals(
        "toDecimalString should return the absolute value for positive input",
        "12345678901234567890",
        positiveWrapper.toDecimalString());

    BigIntegerWrapper negativeWrapper = new BigIntegerWrapper(value.negate());
    assertEquals(
        "toDecimalString should return the absolute value for negative input",
        "12345678901234567890",
        negativeWrapper.toDecimalString());

    BigIntegerWrapper zeroWrapper = new BigIntegerWrapper(BigInteger.ZERO);
    assertEquals(
        "toDecimalString should return '0' for zero input", "0", zeroWrapper.toDecimalString());
  }

  @Test
  public void testToString() {
    BigInteger value = new BigInteger("987654321");
    BigIntegerWrapper positiveWrapper = new BigIntegerWrapper(value);
    assertEquals(
        "toString should return the signed value for positive input",
        "987654321",
        positiveWrapper.toString());

    BigIntegerWrapper negativeWrapper = new BigIntegerWrapper(value.negate());
    assertEquals(
        "toString should return the signed value for negative input",
        "-987654321",
        negativeWrapper.toString());

    BigIntegerWrapper zeroWrapper = new BigIntegerWrapper(BigInteger.ZERO);
    assertEquals("toString should return '0' for zero input", "0", zeroWrapper.toString());
  }

  @Test
  public void testUnsignedConversions_PositiveAndZero() {
    BigInteger value = new BigInteger("255");
    BigIntegerWrapper positiveWrapper = new BigIntegerWrapper(value);
    assertEquals("ff", positiveWrapper.toHexString());
    assertEquals("377", positiveWrapper.toOctalString());
    assertEquals("255", positiveWrapper.toUnsignedDecimalString());

    BigIntegerWrapper zeroWrapper = new BigIntegerWrapper(BigInteger.ZERO);
    assertEquals("0", zeroWrapper.toHexString());
    assertEquals("0", zeroWrapper.toOctalString());
    assertEquals("0", zeroWrapper.toUnsignedDecimalString());
  }

  @Test(expected = PrintfException.class)
  public void testToHexString_NegativeThrowsException() {
    BigIntegerWrapper negativeWrapper = new BigIntegerWrapper(BigInteger.valueOf(-1));
    negativeWrapper.toHexString();
  }

  @Test(expected = PrintfException.class)
  public void testToOctalString_NegativeThrowsException() {
    BigIntegerWrapper negativeWrapper = new BigIntegerWrapper(BigInteger.valueOf(-1));
    negativeWrapper.toOctalString();
  }

  @Test(expected = PrintfException.class)
  public void testToUnsignedDecimalString_NegativeThrowsException() {
    BigIntegerWrapper negativeWrapper = new BigIntegerWrapper(BigInteger.valueOf(-1));
    negativeWrapper.toUnsignedDecimalString();
  }
}
