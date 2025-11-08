package io.fastprintf.number;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ByteWrapperTest {

  @Test
  public void testSignum() {
    assertEquals("Signum of positive byte should be 1", 1, new ByteWrapper((byte) 42).signum());
    assertEquals("Signum of negative byte should be -1", -1, new ByteWrapper((byte) -42).signum());
    assertEquals("Signum of zero byte should be 0", 0, new ByteWrapper((byte) 0).signum());
  }

  @Test
  public void testToDecimalString() {
    assertEquals(
        "toDecimalString should return the absolute value",
        "127",
        new ByteWrapper(Byte.MAX_VALUE).toDecimalString());
    assertEquals(
        "toDecimalString should return the absolute value",
        "128",
        new ByteWrapper(Byte.MIN_VALUE).toDecimalString());
    assertEquals(
        "toDecimalString should return the absolute value",
        "42",
        new ByteWrapper((byte) -42).toDecimalString());
    assertEquals(
        "toDecimalString should return '0'", "0", new ByteWrapper((byte) 0).toDecimalString());
  }

  @Test
  public void testToString() {
    assertEquals(
        "toString should return signed value", "127", new ByteWrapper(Byte.MAX_VALUE).toString());
    assertEquals(
        "toString should return signed value", "-128", new ByteWrapper(Byte.MIN_VALUE).toString());
    assertEquals(
        "toString should return signed value", "-42", new ByteWrapper((byte) -42).toString());
    assertEquals("toString should return '0'", "0", new ByteWrapper((byte) 0).toString());
  }

  @Test
  public void testUnsignedConversions_Positive() {
    ByteWrapper wrapper = new ByteWrapper((byte) 127); // 0x7F
    assertEquals("7f", wrapper.toHexString());
    assertEquals("177", wrapper.toOctalString());
    assertEquals("127", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_Zero() {
    ByteWrapper wrapper = new ByteWrapper((byte) 0); // 0x00
    assertEquals("0", wrapper.toHexString());
    assertEquals("0", wrapper.toOctalString());
    assertEquals("0", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_NegativeOne() {
    // -1 in two's complement is 0xFF, which is 255 unsigned.
    ByteWrapper wrapper = new ByteWrapper((byte) -1);
    assertEquals("ff", wrapper.toHexString());
    assertEquals("377", wrapper.toOctalString());
    assertEquals("255", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_MinValue() {
    // Byte.MIN_VALUE (-128) is 0x80, which is 128 unsigned.
    ByteWrapper wrapper = new ByteWrapper(Byte.MIN_VALUE);
    assertEquals("80", wrapper.toHexString());
    assertEquals("200", wrapper.toOctalString());
    assertEquals("128", wrapper.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_NegativeValue() {
    // -42 in two's complement is 0xD6, which is 214 unsigned.
    ByteWrapper wrapper = new ByteWrapper((byte) -42);
    assertEquals("d6", wrapper.toHexString());
    assertEquals("326", wrapper.toOctalString());
    assertEquals("214", wrapper.toUnsignedDecimalString());
  }
}
