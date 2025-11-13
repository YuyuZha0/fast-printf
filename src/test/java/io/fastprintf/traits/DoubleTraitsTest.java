package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import org.junit.Test;

public class DoubleTraitsTest {

  private static final double DELTA = 1e-9;

  @Test
  public void testOfPrimitive_createsNewInstances() {
    double primitiveValue = 123.45;
    DoubleTraits traits1 = DoubleTraits.ofPrimitive(primitiveValue);
    DoubleTraits traits2 = DoubleTraits.ofPrimitive(primitiveValue);

    assertNotSame("ofPrimitive should create new instances, not cache them", traits1, traits2);
    assertTrue("ref() should indicate a primitive source", traits1.ref().isPrimitive());

    Object obj = traits1.asObject();
    assertTrue("asObject() should return a boxed Double", obj instanceof Double);
    assertEquals(primitiveValue, (Double) obj, DELTA);
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    Double boxedValue = new Double(-98.76);
    DoubleTraits traits = new DoubleTraits(boxedValue, RefSlot.of(boxedValue));

    FloatForm floatForm = traits.asFloatForm();
    assertTrue(floatForm.isNegative());
    assertEquals("-98.76", floatForm.toString());

    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testAsString() {
    assertEquals("123.45", DoubleTraits.ofPrimitive(123.45).asString());
    assertEquals("NaN", DoubleTraits.ofPrimitive(Double.NaN).asString());
    assertEquals("Infinity", DoubleTraits.ofPrimitive(Double.POSITIVE_INFINITY).asString());
    assertEquals("-Infinity", DoubleTraits.ofPrimitive(Double.NEGATIVE_INFINITY).asString());
  }

  @Test
  public void testAsInt_roundsValue() {
    // Round down
    assertEquals(1, DoubleTraits.ofPrimitive(1.49).asInt());
    assertEquals(-1, DoubleTraits.ofPrimitive(-1.49).asInt());
    // Round up (at .5 boundary)
    assertEquals(2, DoubleTraits.ofPrimitive(1.5).asInt());
    assertEquals(2, DoubleTraits.ofPrimitive(1.99).asInt());
    // Negative rounding at .5 (Math.round(-1.5) is -1)
    assertEquals(-1, DoubleTraits.ofPrimitive(-1.5).asInt());
    assertEquals(-2, DoubleTraits.ofPrimitive(-1.51).asInt());
  }

  @Test
  public void testAsInt_handlesOutOfRange_byTruncatingLong() {
    // Math.round(double) returns a long, which is then cast to int.
    // This is a narrowing conversion that truncates the upper 32 bits, NOT a clamping conversion.
    // The test must reflect this actual behavior.

    double largePositiveDouble = (double) Long.MAX_VALUE;
    int expectedPositive = (int) Long.MAX_VALUE; // This is -1
    assertEquals(expectedPositive, DoubleTraits.ofPrimitive(largePositiveDouble).asInt());

    double largeNegativeDouble = (double) Long.MIN_VALUE;
    int expectedNegative = (int) Long.MIN_VALUE; // This is 0
    assertEquals(expectedNegative, DoubleTraits.ofPrimitive(largeNegativeDouble).asInt());
  }

  @Test
  public void testAsIntForm_truncatesValue() {
    // Unlike asInt(), asIntForm() should truncate
    IntForm intForm1 = DoubleTraits.ofPrimitive(1.99).asIntForm();
    assertEquals("1", intForm1.toDecimalString());

    IntForm intForm2 = DoubleTraits.ofPrimitive(-1.99).asIntForm();
    assertEquals("1", intForm2.toDecimalString());
    assertEquals(-1, intForm2.signum());
  }

  @Test
  public void testAsChar_roundsThenCasts() {
    // asChar() inherits from asInt(), which rounds.
    assertEquals('A', DoubleTraits.ofPrimitive(65.4).asChar()); // Rounds down to 65
    assertEquals('B', DoubleTraits.ofPrimitive(65.7).asChar()); // Rounds up to 66
  }

  @Test
  public void testAsFloatForm() {
    double value = -123.456;
    DoubleTraits traits = DoubleTraits.ofPrimitive(value);
    FloatForm floatForm = traits.asFloatForm();

    assertNotNull(floatForm);
    assertEquals(value, Double.parseDouble(floatForm.toString()), DELTA);
    assertTrue(floatForm.isNegative());
  }

  @Test
  public void testEdgeCases_NaN() {
    DoubleTraits traits = DoubleTraits.ofPrimitive(Double.NaN);
    // Math.round(NaN) is 0L, (int)0L is 0
    assertEquals(0, traits.asInt());

    IntForm intForm = traits.asIntForm();
    // (long)NaN is 0L
    assertEquals("0", intForm.toDecimalString());
    assertEquals(0, intForm.signum());

    assertTrue(traits.asFloatForm().isNaN());
  }

  @Test
  public void testEdgeCases_PositiveInfinity() {
    DoubleTraits traits = DoubleTraits.ofPrimitive(Double.POSITIVE_INFINITY);
    // Math.round(+Infinity) is Long.MAX_VALUE. (int)Long.MAX_VALUE is -1.
    assertEquals(-1, traits.asInt());

    // (long)+Infinity is Long.MAX_VALUE
    IntForm intForm = traits.asIntForm();
    assertEquals(String.valueOf(Long.MAX_VALUE), intForm.toString());

    assertTrue(traits.asFloatForm().isInfinite());
    assertTrue(traits.asFloatForm().isPositive());
  }

  @Test
  public void testEdgeCases_NegativeInfinity() {
    DoubleTraits traits = DoubleTraits.ofPrimitive(Double.NEGATIVE_INFINITY);
    // Math.round(-Infinity) is Long.MIN_VALUE. (int)Long.MIN_VALUE is 0.
    assertEquals(0, traits.asInt());

    // (long)-Infinity is Long.MIN_VALUE
    IntForm intForm = traits.asIntForm();
    assertEquals(String.valueOf(Long.MIN_VALUE), intForm.toString());

    assertTrue(traits.asFloatForm().isInfinite());
    assertTrue(traits.asFloatForm().isNegative());
  }

  // --- Test Unsupported Operations ---

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    DoubleTraits.ofPrimitive(123.45).asTemporalAccessor();
  }
}
