package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatLayout;
import io.fastprintf.seq.Seq;
import org.junit.Test;

public class BooleanTraitsTest {

  @Test
  public void testAsSeq_Optimization() {
    // Verify that we are returning static constants (Zero Allocation)
    BooleanTraits t1 = BooleanTraits.ofPrimitive(true);
    BooleanTraits t2 = BooleanTraits.ofPrimitive(true);

    Seq seq1 = t1.asSeq();
    Seq seq2 = t2.asSeq();

    // Must be the exact same instance
    assertSame("Should return cached Seq instance for true", seq1, seq2);

    BooleanTraits f1 = BooleanTraits.ofPrimitive(false);
    Seq seq3 = f1.asSeq();

    assertSame(
        "Should return cached Seq instance for false",
        seq3,
        BooleanTraits.ofPrimitive(false).asSeq());
  }

  @Test
  public void testAsSeq_Content() {
    Seq trueSeq = BooleanTraits.ofPrimitive(true).asSeq();
    assertEquals("true", trueSeq.toString());
    assertEquals(4, trueSeq.length());

    Seq falseSeq = BooleanTraits.ofPrimitive(false).asSeq();
    assertEquals("false", falseSeq.toString());
    assertEquals(5, falseSeq.length());
  }

  @Test
  public void testAsSeq_AppendTo() {
    StringBuilder sb = new StringBuilder();
    BooleanTraits.ofPrimitive(true).asSeq().appendTo(sb);
    BooleanTraits.ofPrimitive(false).asSeq().appendTo(sb);

    assertEquals("truefalse", sb.toString());
  }

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
