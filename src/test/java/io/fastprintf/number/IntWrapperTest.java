package io.fastprintf.number;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IntWrapperTest {

  @Test
  public void testSignum() {
    assertEquals("Signum of positive int should be 1", 1, new IntWrapper(42).signum());
    assertEquals("Signum of negative int should be -1", -1, new IntWrapper(-42).signum());
    assertEquals("Signum of zero should be 0", 0, new IntWrapper(0).signum());
  }

  @Test
  public void testToDecimalString() {
    assertEquals(
        "toDecimalString for positive value", "12345", new IntWrapper(12345).toDecimalString());
    assertEquals(
        "toDecimalString for negative value", "12345", new IntWrapper(-12345).toDecimalString());
    assertEquals("toDecimalString for zero", "0", new IntWrapper(0).toDecimalString());
    assertEquals(
        "toDecimalString for Integer.MAX_VALUE",
        "2147483647",
        new IntWrapper(Integer.MAX_VALUE).toDecimalString());
    assertEquals(
        "toDecimalString for Integer.MIN_VALUE",
        "2147483648",
        new IntWrapper(Integer.MIN_VALUE).toDecimalString());
  }

  @Test
  public void testToString() {
    assertEquals("toString for positive value", "12345", new IntWrapper(12345).toString());
    assertEquals("toString for negative value", "-12345", new IntWrapper(-12345).toString());
    assertEquals("toString for zero", "0", new IntWrapper(0).toString());
    assertEquals(
        "toString for Integer.MAX_VALUE",
        "2147483647",
        new IntWrapper(Integer.MAX_VALUE).toString());
    assertEquals(
        "toString for Integer.MIN_VALUE",
        "-2147483648",
        new IntWrapper(Integer.MIN_VALUE).toString());
  }

  @Test
  public void testUnsignedConversions_Positive() {
    IntWrapper wrapper = new IntWrapper(255);
    assertEquals("ff", wrapper.toHexString());
    assertEquals("377", wrapper.toOctalString());
    assertEquals("255", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_NegativeOne() {
    // -1 in two's complement is 0xFFFFFFFF
    IntWrapper wrapper = new IntWrapper(-1);
    assertEquals("ffffffff", wrapper.toHexString());
    assertEquals("37777777777", wrapper.toOctalString());
    assertEquals("4294967295", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MaxValue() {
    IntWrapper wrapper = new IntWrapper(Integer.MAX_VALUE);
    assertEquals("7fffffff", wrapper.toHexString());
    assertEquals("17777777777", wrapper.toOctalString());
    assertEquals("2147483647", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MinValue() {
    IntWrapper wrapper = new IntWrapper(Integer.MIN_VALUE);
    assertEquals("80000000", wrapper.toHexString());
    assertEquals("20000000000", wrapper.toOctalString());
    assertEquals("2147483648", wrapper.toUnsignedDecimalString());
  }
}
