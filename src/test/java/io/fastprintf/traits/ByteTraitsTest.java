package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.FloatLayout;
import io.fastprintf.number.IntForm;
import org.junit.Test;

public class ByteTraitsTest {

  @Test
  public void testOfPrimitive_createsPrimitiveTraits() {
    byte primitiveValue = 42;
    ByteTraits traits = ByteTraits.ofPrimitive(primitiveValue);

    assertEquals(primitiveValue, traits.asInt());
    assertTrue("ref() should indicate a primitive source", traits.ref().isPrimitive());

    Object obj = traits.asObject();
    assertTrue("asObject() should return a boxed Byte", obj instanceof Byte);
    assertEquals(primitiveValue, ((Byte) obj).byteValue());
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    Byte boxedValue = new Byte((byte) -100);
    ByteTraits traits = new ByteTraits(boxedValue, RefSlot.of(boxedValue));

    assertEquals(boxedValue.byteValue(), traits.asInt());
    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testConversions_PositiveValue() {
    byte value = 65; // ASCII 'A'
    ByteTraits traits = ByteTraits.ofPrimitive(value);

    assertEquals("65", traits.asString());
    assertEquals(65, traits.asInt());
    assertEquals('A', traits.asChar());

    // Correct way to test FloatForm
    FloatLayout layout = traits.asFloatForm().decimalLayout(1);
    assertEquals("65", layout.getMantissa().toString()); // Check the string representation
    assertNull(layout.getExponent());

    assertEquals("65", traits.asIntForm().toDecimalString());
  }

  @Test
  public void testConversions_NegativeValue() {
    byte value = -1;
    ByteTraits traits = ByteTraits.ofPrimitive(value);

    assertEquals("-1", traits.asString());
    assertEquals(-1, traits.asInt());
    // (char)-1 is 0xFFFF
    assertEquals((char) 0xFFFF, traits.asChar());

    // Correct way to test FloatForm
    FloatForm floatForm = traits.asFloatForm();
    assertTrue(floatForm.isNegative());
    FloatLayout layout = floatForm.decimalLayout(1);
    assertEquals("1", layout.getMantissa().toString()); // The mantissa is the absolute value part

    IntForm intForm = traits.asIntForm();
    assertEquals("1", intForm.toDecimalString()); // Absolute value
    assertEquals(-1, intForm.signum());
  }

  @Test
  public void testConversions_Zero() {
    byte value = 0;
    ByteTraits traits = ByteTraits.ofPrimitive(value);

    assertEquals("0", traits.asString());
    assertEquals(0, traits.asInt());
    assertEquals('\0', traits.asChar());

    // Correct way to test FloatForm
    FloatLayout layout = traits.asFloatForm().decimalLayout(1);
    assertEquals("0", layout.getMantissa().toString());

    assertEquals("0", traits.asIntForm().toDecimalString());
  }

  @Test
  public void testUnsignedConversions_NegativeByte() {
    // -1 as a byte is 0xFF in two's complement.
    byte value = -1;
    ByteTraits traits = ByteTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    // Byte.toUnsignedInt(-1) is 255
    assertEquals("ff", intForm.toHexString());
    assertEquals("377", intForm.toOctalString());
    assertEquals("255", intForm.toUnsignedDecimalString());
  }

  @Test
  public void testUnsignedConversions_PositiveByte() {
    byte value = 127; // 0x7F
    ByteTraits traits = ByteTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    assertEquals("7f", intForm.toHexString());
    assertEquals("177", intForm.toOctalString());
    assertEquals("127", intForm.toUnsignedDecimalString());
  }

  @Test
  public void testEdgeCases_MinValue() {
    byte value = Byte.MIN_VALUE; // -128
    ByteTraits traits = ByteTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    assertEquals("-128", traits.asString());
    assertEquals(-128, traits.asInt());

    // Unsigned conversions for -128 (0x80)
    assertEquals("80", intForm.toHexString());
    assertEquals("200", intForm.toOctalString());
    assertEquals("128", intForm.toUnsignedDecimalString());
  }

  @Test
  public void testEdgeCases_MaxValue() {
    byte value = Byte.MAX_VALUE; // 127
    ByteTraits traits = ByteTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    assertEquals("127", traits.asString());
    assertEquals(127, traits.asInt());

    // Unsigned conversions for 127 (0x7F)
    assertEquals("7f", intForm.toHexString());
    assertEquals("177", intForm.toOctalString());
    assertEquals("127", intForm.toUnsignedDecimalString());
  }
}
