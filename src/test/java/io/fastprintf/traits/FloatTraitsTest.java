package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import org.junit.Test;

public class FloatTraitsTest {

  private static final float DELTA = 1e-6f;

  @Test
  public void testOfPrimitive_createsNewInstances() {
    float primitiveValue = 123.45f;
    FloatTraits traits1 = FloatTraits.ofPrimitive(primitiveValue);
    FloatTraits traits2 = FloatTraits.ofPrimitive(primitiveValue);

    assertNotSame("ofPrimitive should create new instances, not cache them", traits1, traits2);
    assertTrue("ref() should indicate a primitive source", traits1.ref().isPrimitive());

    Object obj = traits1.asObject();
    assertTrue("asObject() should return a boxed Float", obj instanceof Float);
    assertEquals(primitiveValue, (Float) obj, DELTA);
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    Float boxedValue = new Float(-98.76f);
    FloatTraits traits = new FloatTraits(boxedValue, RefSlot.of(boxedValue));

    FloatForm floatForm = traits.asFloatForm();
    assertTrue(floatForm.isNegative());
    assertEquals("98.76", floatForm.decimalLayout(2).getMantissa().toString());

    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testAsString() {
    assertEquals("123.45", FloatTraits.ofPrimitive(123.45f).asString());
    assertEquals("NaN", FloatTraits.ofPrimitive(Float.NaN).asString());
    assertEquals("Infinity", FloatTraits.ofPrimitive(Float.POSITIVE_INFINITY).asString());
    assertEquals("-Infinity", FloatTraits.ofPrimitive(Float.NEGATIVE_INFINITY).asString());
  }

  @Test
  public void testAsInt_roundsValue() {
    // Round down
    assertEquals(1, FloatTraits.ofPrimitive(1.49f).asInt());
    assertEquals(-1, FloatTraits.ofPrimitive(-1.49f).asInt());
    // Round up (at .5 boundary)
    assertEquals(2, FloatTraits.ofPrimitive(1.5f).asInt());
    assertEquals(2, FloatTraits.ofPrimitive(1.99f).asInt());
    // Negative rounding at .5 (Math.round(-1.5) is -1)
    assertEquals(-1, FloatTraits.ofPrimitive(-1.5f).asInt());
    assertEquals(-2, FloatTraits.ofPrimitive(-1.51f).asInt());
  }

  @Test
  public void testAsIntForm_truncatesValue() {
    // Unlike asInt(), asIntForm() should truncate
    IntForm intForm1 = FloatTraits.ofPrimitive(1.99f).asIntForm();
    assertEquals("1", intForm1.toDecimalString());

    IntForm intForm2 = FloatTraits.ofPrimitive(-1.99f).asIntForm();
    assertEquals("1", intForm2.toDecimalString());
    assertEquals(-1, intForm2.signum());
  }

  @Test
  public void testAsChar_roundsThenCasts() {
    // asChar() inherits from asInt(), which rounds.
    assertEquals('A', FloatTraits.ofPrimitive(65.4f).asChar()); // Rounds down to 65
    assertEquals('B', FloatTraits.ofPrimitive(65.7f).asChar()); // Rounds up to 66
  }

  @Test
  public void testEdgeCases_NaN() {
    FloatTraits traits = FloatTraits.ofPrimitive(Float.NaN);
    assertEquals(0, traits.asInt());

    IntForm intForm = traits.asIntForm();
    assertEquals("0", intForm.toDecimalString());
    assertEquals(0, intForm.signum());

    assertTrue(traits.asFloatForm().isNaN());
  }

  @Test
  public void testEdgeCases_PositiveInfinity() {
    FloatTraits traits = FloatTraits.ofPrimitive(Float.POSITIVE_INFINITY);
    assertEquals(Integer.MAX_VALUE, traits.asInt());

    IntForm intForm = traits.asIntForm();
    assertEquals(Integer.MAX_VALUE, Integer.parseInt(intForm.toString()));

    assertTrue(traits.asFloatForm().isInfinite());
    assertTrue(traits.asFloatForm().isPositive());
  }

  @Test
  public void testEdgeCases_NegativeInfinity() {
    FloatTraits traits = FloatTraits.ofPrimitive(Float.NEGATIVE_INFINITY);
    assertEquals(Integer.MIN_VALUE, traits.asInt());

    IntForm intForm = traits.asIntForm();
    assertEquals(Integer.MIN_VALUE, Integer.parseInt(intForm.toString()));

    assertTrue(traits.asFloatForm().isInfinite());
    assertTrue(traits.asFloatForm().isNegative());
  }

  // --- Test Unsupported Operations ---

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    FloatTraits.ofPrimitive(123.45f).asTemporalAccessor();
  }
}
