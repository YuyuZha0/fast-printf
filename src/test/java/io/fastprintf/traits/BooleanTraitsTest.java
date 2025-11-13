package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatLayout;
import org.junit.Test;

public class BooleanTraitsTest {

  @Test
  public void testOfPrimitive_returnsCachedInstances() {
    BooleanTraits trueTraits1 = BooleanTraits.ofPrimitive(true);
    BooleanTraits trueTraits2 = BooleanTraits.ofPrimitive(true);
    assertSame("Expected cached TRUE instance", trueTraits1, trueTraits2);

    BooleanTraits falseTraits1 = BooleanTraits.ofPrimitive(false);
    BooleanTraits falseTraits2 = BooleanTraits.ofPrimitive(false);
    assertSame("Expected cached FALSE instance", falseTraits1, falseTraits2);
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    // Use the canonical boxed object
    Boolean boxedValue = Boolean.TRUE;
    BooleanTraits traits = new BooleanTraits(boxedValue, RefSlot.of(boxedValue));

    assertTrue(traits.asInt() == 1);
    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testConversions_True() {
    BooleanTraits traits = BooleanTraits.ofPrimitive(true);

    assertEquals("true", traits.asString());
    assertEquals(1, traits.asInt());
    assertEquals((char) 1, traits.asChar());

    assertEquals(1, traits.asIntForm().signum());
    assertEquals("1", traits.asIntForm().toDecimalString());

    // Correct way to test FloatForm
    FloatLayout layout = traits.asFloatForm().decimalLayout(1);
    assertEquals("1", layout.getMantissa().toString());
  }

  @Test
  public void testConversions_False() {
    BooleanTraits traits = BooleanTraits.ofPrimitive(false);

    assertEquals("false", traits.asString());
    assertEquals(0, traits.asInt());
    assertEquals((char) 0, traits.asChar()); // The null character

    assertEquals(0, traits.asIntForm().signum());
    assertEquals("0", traits.asIntForm().toDecimalString());

    // Correct way to test FloatForm
    FloatLayout layout = traits.asFloatForm().decimalLayout(1);
    assertEquals("0", layout.getMantissa().toString());
  }

  @Test
  public void testRefAndAsObjectBehavior() {
    // --- Primitive Case ---
    BooleanTraits primitiveTraits = BooleanTraits.ofPrimitive(true);
    assertTrue("ref() should be primitive for ofPrimitive()", primitiveTraits.ref().isPrimitive());

    Object primitiveAsObject = primitiveTraits.asObject();
    assertTrue(
        "asObject() from primitive should return a Boolean", primitiveAsObject instanceof Boolean);
    assertEquals(true, primitiveAsObject);

    // --- Boxed Case ---
    Boolean originalBoxedFalse = Boolean.FALSE;
    BooleanTraits boxedTraits =
        new BooleanTraits(originalBoxedFalse, RefSlot.of(originalBoxedFalse));
    assertFalse(
        "ref() should not be primitive for a boxed Boolean", boxedTraits.ref().isPrimitive());

    Object boxedAsObject = boxedTraits.asObject();
    assertSame(
        "asObject() from boxed should return the identical instance",
        originalBoxedFalse,
        boxedAsObject);
  }

  // --- Test Unsupported Operations ---

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    BooleanTraits.ofPrimitive(true).asTemporalAccessor();
  }
}
