package io.fastprintf.number;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LongWrapperTest {

  @Test
  public void testSignum() {
    assertEquals("Signum of positive long should be 1", 1, new LongWrapper(42L).signum());
    assertEquals("Signum of negative long should be -1", -1, new LongWrapper(-42L).signum());
    assertEquals("Signum of zero should be 0", 0, new LongWrapper(0L).signum());
  }

  @Test
  public void testToDecimalString() {
    assertEquals(
        "toDecimalString for positive value", "12345", new LongWrapper(12345L).toDecimalString());
    assertEquals(
        "toDecimalString for negative value", "12345", new LongWrapper(-12345L).toDecimalString());
    assertEquals("toDecimalString for zero", "0", new LongWrapper(0L).toDecimalString());
    assertEquals(
        "toDecimalString for Long.MAX_VALUE",
        "9223372036854775807",
        new LongWrapper(Long.MAX_VALUE).toDecimalString());
    assertEquals(
        "toDecimalString for Long.MIN_VALUE",
        "9223372036854775808",
        new LongWrapper(Long.MIN_VALUE).toDecimalString());
  }

  @Test
  public void testToString() {
    assertEquals("toString for positive value", "12345", new LongWrapper(12345L).toString());
    assertEquals("toString for negative value", "-12345", new LongWrapper(-12345L).toString());
    assertEquals("toString for zero", "0", new LongWrapper(0L).toString());
    assertEquals(
        "toString for Long.MAX_VALUE",
        "9223372036854775807",
        new LongWrapper(Long.MAX_VALUE).toString());
    assertEquals(
        "toString for Long.MIN_VALUE",
        "-9223372036854775808",
        new LongWrapper(Long.MIN_VALUE).toString());
  }

  @Test
  public void testUnsignedConversions_Positive() {
    LongWrapper wrapper = new LongWrapper(255L);
    assertEquals("ff", wrapper.toHexString());
    assertEquals("377", wrapper.toOctalString());
    assertEquals("255", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_NegativeOne() {
    // -1L in two's complement is 0xFFFFFFFFFFFFFFFF
    LongWrapper wrapper = new LongWrapper(-1L);
    assertEquals("ffffffffffffffff", wrapper.toHexString());
    assertEquals("1777777777777777777777", wrapper.toOctalString());
    assertEquals("18446744073709551615", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MaxValue() {
    LongWrapper wrapper = new LongWrapper(Long.MAX_VALUE);
    assertEquals("7fffffffffffffff", wrapper.toHexString());
    assertEquals("777777777777777777777", wrapper.toOctalString());
    assertEquals("9223372036854775807", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MinValue() {
    LongWrapper wrapper = new LongWrapper(Long.MIN_VALUE);
    assertEquals("8000000000000000", wrapper.toHexString());
    assertEquals("1000000000000000000000", wrapper.toOctalString());
    assertEquals("9223372036854775808", wrapper.toUnsignedDecimalString());
  }
}
