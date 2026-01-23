package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.seq.Seq;
import org.junit.Test;

public class NullTraitsTest {

  @Test
  public void testAsSeq_Optimization() {
    NullTraits t1 = NullTraits.getInstance();
    NullTraits t2 = NullTraits.getInstance();

    Seq seq1 = t1.asSeq();
    Seq seq2 = t2.asSeq();

    // Verify constant reuse
    assertSame("Should return the exact same Seq instance for null", seq1, seq2);
  }

  @Test
  public void testAsSeq_Content() {
    Seq seq = NullTraits.getInstance().asSeq();
    assertEquals("null", seq.toString());
    assertEquals(4, seq.length());
  }

  @Test
  public void testAsSeq_AppendTo() {
    StringBuilder sb = new StringBuilder();
    NullTraits.getInstance().asSeq().appendTo(sb);
    assertEquals("null", sb.toString());
  }

  @Test
  public void testGetInstance_isSingleton() {
    NullTraits instance1 = NullTraits.getInstance();
    NullTraits instance2 = NullTraits.getInstance();
    assertSame(
        "getInstance() should always return the same singleton instance", instance1, instance2);
  }

  @Test
  public void testIsNull() {
    assertTrue("isNull() must always return true", NullTraits.getInstance().isNull());
  }

  @Test
  public void testAsString() {
    assertEquals("null", NullTraits.getInstance().asString());
  }

  @Test
  public void testRef() {
    RefSlot ref = NullTraits.getInstance().ref();
    assertNotNull(ref);
    assertFalse("RefSlot for null should not be primitive", ref.isPrimitive());
    // A direct check for the singleton instance from RefSlot
    assertSame(RefSlot.ofNull(), ref);
  }

  @Test
  public void testAsObject() {
    // The default implementation of asObject() on FormatTraits will throw
    // an UnsupportedOperationException if ref().isPrimitive() is true,
    // and will call ref().get() otherwise. For a null ref, get() returns null.
    // We need to verify this behavior.
    assertNull("asObject() for NullTraits should return null", NullTraits.getInstance().asObject());
  }

  // --- Test All Unsupported Conversions ---

  @Test(expected = PrintfException.class)
  public void testAsInt_throwsException() {
    NullTraits.getInstance().asInt();
  }

  @Test(expected = PrintfException.class)
  public void testAsChar_throwsException() {
    // This relies on the default asChar() calling the failing asInt()
    NullTraits.getInstance().asChar();
  }

  @Test(expected = PrintfException.class)
  public void testAsIntForm_throwsException() {
    NullTraits.getInstance().asIntForm();
  }

  @Test(expected = PrintfException.class)
  public void testAsFloatForm_throwsException() {
    NullTraits.getInstance().asFloatForm();
  }

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    NullTraits.getInstance().asTemporalAccessor();
  }
}
