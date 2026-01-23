package io.fastprintf.seq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.junit.Test;

public class LazySeqTest {

  // --- Static Helper Tests (buildEagerly) ---

  @Test
  public void testBuildEagerlyReturnsStringBuilder() {
    // The new signature returns StringBuilder, not String
    StringBuilder result = LazySeq.buildEagerly(sb -> sb.append("123"), 3);

    assertNotNull(result);
    assertEquals("123", result.toString());
    assertEquals(3, result.length());
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildEagerlyThrowsOnShortLength() {
    // Promised 5, wrote 3
    LazySeq.buildEagerly(sb -> sb.append("123"), 5);
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildEagerlyThrowsOnLongLength() {
    // Promised 3, wrote 5
    LazySeq.buildEagerly(sb -> sb.append("12345"), 3);
  }

  // --- Instance Method Tests ---

  @Test
  public void testLength() {
    LazySeq seq = new LazySeq(sb -> sb.append("test"), 4);
    // Length must be returned without running the action
    assertEquals(4, seq.length());
  }

  @Test
  public void testToStringSuccess() {
    LazySeq seq = new LazySeq(sb -> sb.append("foo"), 3);
    // toString calls buildEagerly and converts to String
    assertEquals("foo", seq.toString());
  }

  @Test
  public void testAppendToFastPath() {
    // The fast path (StringBuilder) simply executes the action.
    // Ideally, this path does not trigger the internal 'buildEagerly' check,
    // maximizing performance.
    LazySeq seq = new LazySeq(sb -> sb.append("fast"), 4);

    StringBuilder sb = new StringBuilder();
    seq.appendTo(sb);

    assertEquals("fast", sb.toString());
  }

  @Test
  public void testAppendToGenericPath() throws IOException {
    // The generic path (Appendable) hydrates via toString() -> buildEagerly
    LazySeq seq = new LazySeq(sb -> sb.append("generic"), 7);

    StringBuilder sb = new StringBuilder();
    seq.appendTo((Appendable) sb);

    assertEquals("generic", sb.toString());
  }

  @Test
  public void testCharAt() {
    // charAt now operates on the StringBuilder returned by buildEagerly
    LazySeq seq = new LazySeq(sb -> sb.append("ABC"), 3);
    assertEquals('A', seq.charAt(0));
    assertEquals('B', seq.charAt(1));
    assertEquals('C', seq.charAt(2));
  }

  @Test
  public void testSubSequence() {
    LazySeq seq = new LazySeq(sb -> sb.append("012345"), 6);

    // The new implementation calls buildEagerly().subSequence(...).toString()
    // and wraps it in a Seq.
    AtomicSeq sub = seq.subSequence(2, 4);

    assertEquals(2, sub.length());
    assertEquals("23", sub.toString());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSubSequenceOutOfBounds() {
    LazySeq seq = new LazySeq(sb -> sb.append("123"), 3);
    seq.subSequence(0, 4); // Index 4 is out of bounds
  }

  @Test
  public void testUpperCase() {
    // upperCase() usually delegates to Seq.wrap(toString()).upperCase()
    LazySeq seq = new LazySeq(sb -> sb.append("lower"), 5);

    Seq upper = seq.upperCase();

    assertEquals("LOWER", upper.toString());
  }

  @Test
  public void testComplexActionAndVariableCapture() {
    // Verify functionality with closures
    int id = 42;
    String name = "Test";

    // "ID: 42, Name: Test" -> length 18
    LazySeq seq =
        new LazySeq(
            sb -> {
              sb.append("ID: ").append(id).append(", Name: ").append(name);
            },
            18);

    assertEquals("ID: 42, Name: Test", seq.toString());
  }
}
