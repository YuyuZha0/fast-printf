package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.FloatLayout;
import io.fastprintf.number.IntForm;
import org.junit.Test;

public class ShortTraitsTest {

  @Test
  public void testOfPrimitive_createsPrimitiveTraits() {
    short primitiveValue = 1234;
    ShortTraits traits = ShortTraits.ofPrimitive(primitiveValue);

    assertEquals(primitiveValue, traits.asInt());
    assertTrue("ref() should indicate a primitive source", traits.ref().isPrimitive());

    Object obj = traits.asObject();
    assertTrue("asObject() should return a boxed Short", obj instanceof Short);
    assertEquals(primitiveValue, ((Short) obj).shortValue());
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    Short boxedValue = new Short((short) -5678);
    ShortTraits traits = new ShortTraits(boxedValue, RefSlot.of(boxedValue));

    assertEquals(boxedValue.shortValue(), traits.asInt());
    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testConversions_PositiveValue() {
    short value = 65; // ASCII 'A'
    ShortTraits traits = ShortTraits.ofPrimitive(value);

    assertEquals("65", traits.asString());
    assertEquals(65, traits.asInt());
    assertEquals('A', traits.asChar());

    // Correct way to test FloatForm
    FloatLayout layout = traits.asFloatForm().decimalLayout(1);
    assertEquals("65", layout.getMantissa().toString());
    assertNull(layout.getExponent());

    assertEquals("65", traits.asIntForm().toDecimalString());
  }

  @Test
  public void testConversions_NegativeValue() {
    short value = -1;
    ShortTraits traits = ShortTraits.ofPrimitive(value);

    assertEquals("-1", traits.asString());
    assertEquals(-1, traits.asInt());
    // (char)-1 is 0xFFFF
    assertEquals((char) 0xFFFF, traits.asChar());

    // Correct way to test FloatForm
    FloatForm floatForm = traits.asFloatForm();
    assertTrue(floatForm.isNegative());
    FloatLayout layout = floatForm.decimalLayout(1);
    assertEquals("1", layout.getMantissa().toString());

    IntForm intForm = traits.asIntForm();
    assertEquals("1", intForm.toDecimalString()); // Absolute value
    assertEquals(-1, intForm.signum());
  }

  @Test
  public void testUnsignedConversions_NegativeShort() {
    // -1 as a short is 0xFFFF in two's complement.
    short value = -1;
    ShortTraits traits = ShortTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    // Short.toUnsignedInt(-1) is 65535
    assertEquals("ffff", intForm.toHexString());
    assertEquals("177777", intForm.toOctalString());
    assertEquals("65535", intForm.toUnsignedDecimalString());
  }

  @Test
  public void testEdgeCases_MinValue() {
    short value = Short.MIN_VALUE; // -32768
    ShortTraits traits = ShortTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    assertEquals("-32768", traits.asString());
    assertEquals(-32768, traits.asInt());

    // Unsigned conversions for -32768 (0x8000)
    assertEquals("8000", intForm.toHexString());
    assertEquals("100000", intForm.toOctalString());
    assertEquals("32768", intForm.toUnsignedDecimalString());
  }

  @Test
  public void testEdgeCases_MaxValue() {
    short value = Short.MAX_VALUE; // 32767
    ShortTraits traits = ShortTraits.ofPrimitive(value);
    IntForm intForm = traits.asIntForm();

    assertEquals("32767", traits.asString());
    assertEquals(32767, traits.asInt());

    // Unsigned conversions for 32767 (0x7FFF)
    assertEquals("7fff", intForm.toHexString());
    assertEquals("77777", intForm.toOctalString());
    assertEquals("32767", intForm.toUnsignedDecimalString());
  }

  // --- Test Unsupported Operations ---

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    ShortTraits.ofPrimitive((short) 123).asTemporalAccessor();
  }
}
