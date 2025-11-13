package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import java.math.BigDecimal;
import org.junit.Test;

public class BigDecimalTraitsTest {

  @Test
  public void testConstructorAndRef() {
    BigDecimal bd = new BigDecimal("123.456");
    BigDecimalTraits traits = new BigDecimalTraits(bd);

    assertNotNull("Traits should not be null", traits);
    RefSlot ref = traits.ref();
    assertFalse("RefSlot should not be primitive", ref.isPrimitive());
    assertSame("RefSlot should hold the original BigDecimal object", bd, ref.get());
  }

  @Test
  public void testAsString() {
    BigDecimal bd = new BigDecimal("-9876543210.123456789");
    BigDecimalTraits traits = new BigDecimalTraits(bd);
    assertEquals("-9876543210.123456789", traits.asString());

    BigDecimal bdInt = new BigDecimal("500");
    BigDecimalTraits traitsInt = new BigDecimalTraits(bdInt);
    assertEquals("500", traitsInt.asString());
  }

  @Test
  public void testAsInt() {
    // Test with a value that fits in an int
    BigDecimal bd1 = new BigDecimal("12345.67");
    BigDecimalTraits traits1 = new BigDecimalTraits(bd1);
    assertEquals(12345, traits1.asInt());

    // Test with a value that will overflow an int
    BigDecimal bd2 = new BigDecimal("1234567890123.45");
    BigDecimalTraits traits2 = new BigDecimalTraits(bd2);
    // intValue() behavior is to return the low-order 32 bits.
    assertEquals(bd2.intValue(), traits2.asInt());

    // Test with a negative value
    BigDecimal bd3 = new BigDecimal("-987.65");
    BigDecimalTraits traits3 = new BigDecimalTraits(bd3);
    assertEquals(-987, traits3.asInt());
  }

  @Test
  public void testAsIntForm() {
    // Value with a fractional part should be truncated
    BigDecimal bd1 = new BigDecimal("9876543210.999");
    BigDecimalTraits traits1 = new BigDecimalTraits(bd1);
    IntForm intForm1 = traits1.asIntForm();
    assertEquals("9876543210", intForm1.toDecimalString());
    assertEquals(1, intForm1.signum());

    // Negative value
    BigDecimal bd2 = new BigDecimal("-123.45");
    BigDecimalTraits traits2 = new BigDecimalTraits(bd2);
    IntForm intForm2 = traits2.asIntForm();
    assertEquals("123", intForm2.toDecimalString());
    assertEquals(-1, intForm2.signum());
  }

  @Test
  public void testAsFloatForm() {
    BigDecimal bd = new BigDecimal("1.23e-10");
    BigDecimalTraits traits = new BigDecimalTraits(bd);
    FloatForm floatForm = traits.asFloatForm();

    assertNotNull("FloatForm should not be null", floatForm);
    // Further testing of FloatForm is complex and belongs in BigDecimalWrapperTest,
    // but we can check the signum.
    assertEquals(1, floatForm.signum());
  }

  @Test
  public void testAsObject() {
    BigDecimal originalValue = new BigDecimal("99.9");
    BigDecimalTraits traits = new BigDecimalTraits(originalValue);
    Object obj = traits.asObject();

    assertSame("asObject() must return the identical BigDecimal instance", originalValue, obj);
  }

  @Test
  public void testAsChar_throwsException() {
    BigDecimal bd = new BigDecimal("65");
    assertEquals('A', new BigDecimalTraits(bd).asChar());
  }

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    BigDecimal bd = new BigDecimal("1640995200");
    new BigDecimalTraits(bd).asTemporalAccessor();
  }
}
