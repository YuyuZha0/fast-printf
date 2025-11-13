package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class EmptySeqTest {

  @Test
  public void testSingletonInstance() {
    // EmptySeq is package-private, so we access it via the public factory
    AtomicSeq instance1 = Seq.empty();
    AtomicSeq instance2 = Seq.empty();
    assertSame(
        "Seq.empty() should always return the same singleton instance", instance1, instance2);
    assertTrue("Instance from Seq.empty() should be EmptySeq", instance1 instanceof EmptySeq);
  }

  @Test
  public void testCoreProperties() {
    Seq empty = Seq.empty();
    assertEquals("length() should be 0", 0, empty.length());
    assertTrue("isEmpty() should be true", empty.isEmpty());
    assertEquals("toString() should be an empty string", "", empty.toString());
    assertEquals("indexOf() should always be -1", -1, empty.indexOf('a'));
    assertEquals("elementCount() should be 1", 1, empty.elementCount());
    assertTrue("isAtomic() should be true", empty.isAtomic());
  }

  @Test
  public void testAppend() {
    Seq empty = Seq.empty();
    Seq other = Seq.wrap("test");
    assertSame("Appending to empty should return the other sequence", other, empty.append(other));
  }

  @Test
  public void testPrepend() {
    Seq empty = Seq.empty();
    Seq other = Seq.wrap("test");
    assertSame("Prepending to empty should return the other sequence", other, empty.prepend(other));
  }

  @Test
  public void testAppendTo_StringBuilder() {
    StringBuilder sb = new StringBuilder("start");
    Seq.empty().appendTo(sb);
    assertEquals("The StringBuilder should not be modified", "start", sb.toString());
  }

  @Test
  public void testAppendTo_Appendable() throws IOException {
    // Use a simple StringBuilder as the Appendable
    StringBuilder sb = new StringBuilder("start");
    Seq.empty().appendTo((Appendable) sb);
    assertEquals("The Appendable should not be modified", "start", sb.toString());
  }

  @Test
  public void testUpperCase() {
    Seq empty = Seq.empty();
    assertSame("upperCase() on empty should return itself", empty, empty.upperCase());
  }

  // --- Exception Tests ---

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsException() {
    Seq.empty().charAt(0);
  }

  @Test
  public void testSubSequence() {
    Seq empty = Seq.empty();
    assertSame("subSequence(0, 0) should return itself", empty, empty.subSequence(0, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSubSequence_throwsForInvalidEnd() {
    Seq.empty().subSequence(0, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSubSequence_throwsForInvalidStart() {
    Seq.empty().subSequence(1, 1);
  }
}
