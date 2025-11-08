package io.fastprintf.number;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShortWrapperTest {

  @Test
  public void testSignum() {
    assertEquals("Signum of positive short should be 1", 1, new ShortWrapper((short) 42).signum());
    assertEquals(
        "Signum of negative short should be -1", -1, new ShortWrapper((short) -42).signum());
    assertEquals("Signum of zero should be 0", 0, new ShortWrapper((short) 0).signum());
  }

  @Test
  public void testToDecimalString() {
    assertEquals(
        "toDecimalString for positive value",
        "12345",
        new ShortWrapper((short) 12345).toDecimalString());
    assertEquals(
        "toDecimalString for negative value",
        "12345",
        new ShortWrapper((short) -12345).toDecimalString());
    assertEquals("toDecimalString for zero", "0", new ShortWrapper((short) 0).toDecimalString());
    assertEquals(
        "toDecimalString for Short.MAX_VALUE",
        "32767",
        new ShortWrapper(Short.MAX_VALUE).toDecimalString());
    assertEquals(
        "toDecimalString for Short.MIN_VALUE",
        "32768",
        new ShortWrapper(Short.MIN_VALUE).toDecimalString());
  }

  @Test
  public void testToString() {
    assertEquals(
        "toString for positive value", "12345", new ShortWrapper((short) 12345).toString());
    assertEquals(
        "toString for negative value", "-12345", new ShortWrapper((short) -12345).toString());
    assertEquals("toString for zero", "0", new ShortWrapper((short) 0).toString());
    assertEquals(
        "toString for Short.MAX_VALUE", "32767", new ShortWrapper(Short.MAX_VALUE).toString());
    assertEquals(
        "toString for Short.MIN_VALUE", "-32768", new ShortWrapper(Short.MIN_VALUE).toString());
  }

  @Test
  public void testUnsignedConversions_Positive() {
    ShortWrapper wrapper = new ShortWrapper((short) 255);
    assertEquals("ff", wrapper.toHexString());
    assertEquals("377", wrapper.toOctalString());
    assertEquals("255", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_NegativeOne() {
    // -1 in two's complement is 0xFFFF, which is 65535 unsigned.
    ShortWrapper wrapper = new ShortWrapper((short) -1);
    assertEquals("ffff", wrapper.toHexString());
    assertEquals("177777", wrapper.toOctalString());
    assertEquals("65535", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MaxValue() {
    ShortWrapper wrapper = new ShortWrapper(Short.MAX_VALUE);
    assertEquals("7fff", wrapper.toHexString());
    assertEquals("77777", wrapper.toOctalString());
    assertEquals("32767", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MinValue() {
    ShortWrapper wrapper = new ShortWrapper(Short.MIN_VALUE);
    assertEquals("8000", wrapper.toHexString());
    assertEquals("100000", wrapper.toOctalString());
    assertEquals("32768", wrapper.toUnsignedDecimalString());
  }
}
