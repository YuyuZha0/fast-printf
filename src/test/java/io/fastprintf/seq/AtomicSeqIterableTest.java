package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AtomicSeqIterableTest {

  private AtomicSeqIterable iterable;

  @Before
  public void setUp() {
    // Our test subject is a composite of three atomic parts: "AB" + "C" + "DEFG"
    iterable = new TestIterable(Seq.wrap("AB"), Seq.wrap("C"), Seq.wrap("DEFG"));
  }

  @Test
  public void test_charAt() {
    assertEquals(7, iterable.length());
    // Within first part
    assertEquals('A', iterable.charAt(0));
    // Boundary between first and second parts
    assertEquals('C', iterable.charAt(2));
    // Boundary between second and third parts
    assertEquals('D', iterable.charAt(3));
    // Within last part
    assertEquals('G', iterable.charAt(6));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void test_charAt_OutOfBounds() {
    iterable.charAt(7);
  }

  @Test
  public void test_subSequence() {
    // Subsequence fully within one part
    assertEquals("DE", iterable.subSequence(3, 5).toString());
    // Subsequence spanning the first two parts
    assertEquals("BC", iterable.subSequence(1, 3).toString());
    // Subsequence spanning all three parts
    assertEquals("BCDEFG", iterable.subSequence(1, 7).toString());
    // Entire sequence
    assertEquals("ABCDEFG", iterable.subSequence(0, 7).toString());
    // Empty subsequence
    assertEquals("", iterable.subSequence(3, 3).toString());
  }

  @Test
  public void test_appendTo() throws IOException {
    StringBuilder sb = new StringBuilder();
    iterable.appendTo(sb);
    assertEquals("ABCDEFG", sb.toString());

    // Test generic Appendable
    StringBuilder sb2 = new StringBuilder();
    iterable.appendTo((Appendable) sb2);
    assertEquals("ABCDEFG", sb2.toString());
  }

  @Test
  public void test_indexOf() {
    assertEquals(0, iterable.indexOf('A')); // First part
    assertEquals(2, iterable.indexOf('C')); // Second part
    assertEquals(4, iterable.indexOf('E')); // Third part
    assertEquals(-1, iterable.indexOf('z')); // Not found
  }

  @Test
  public void test_upperCase() {
    Seq upper = iterable.upperCase();
    // The default implementation uses Seq.join, which returns a SeqArray
    assertTrue(upper instanceof SeqArray);
    assertEquals("ABCDEFG", upper.toString());
    assertEquals(7, upper.length());
  }

  @Test
  public void testUnfold_AbstractMethodCanBeCalled() {
    // This test just ensures our stub's implementation of the abstract method works
    Deque<Seq> stack = new ArrayDeque<>();
    Seq first = iterable.unfold(stack);

    assertEquals("AB", first.toString());
    assertEquals(2, stack.size());
    assertEquals("C", stack.pop().toString());
    assertEquals("DEFG", stack.pop().toString());
  }

  /**
   * A minimal, concrete implementation of AtomicSeqIterable used to test the default methods of the
   * interface.
   */
  private static class TestIterable implements AtomicSeqIterable {
    private final List<AtomicSeq> parts;
    private final int length;

    TestIterable(AtomicSeq... parts) {
      this.parts = Arrays.asList(parts);
      this.length = this.parts.stream().mapToInt(Seq::length).sum();
    }

    @Override
    public Seq unfold(Deque<Seq> stack) {
      // Simple implementation for testing purposes
      for (int i = parts.size() - 1; i >= 1; i--) {
        stack.push(parts.get(i));
      }
      return parts.isEmpty() ? Seq.empty() : parts.get(0);
    }

    @Override
    public int length() {
      return length;
    }

    @Override
    public Iterator<AtomicSeq> iterator() {
      return parts.iterator();
    }

    @Override
    public String toString() {
      StringWriter sw = new StringWriter();
      try {
        for (AtomicSeq part : parts) {
          part.appendTo(sw);
        }
      } catch (IOException e) {
        // Should not happen with StringWriter
      }
      return sw.toString();
    }
  }
}
