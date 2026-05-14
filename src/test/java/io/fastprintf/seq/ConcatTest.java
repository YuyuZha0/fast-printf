package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Test;

public class ConcatTest {

  @Test
  public void testBasicConcatenation() {
    Seq hello = Seq.wrap("Hello");
    Seq world = Seq.wrap(" World");
    Concat seq = Concat.concat(hello, world);

    assertEquals(11, seq.length());
    assertEquals(2, seq.elementCount());
    assertEquals("Hello World", seq.toString());

    assertEquals('H', seq.charAt(0));
    assertEquals('o', seq.charAt(4));
    assertEquals(' ', seq.charAt(5)); // Boundary
    assertEquals('d', seq.charAt(10));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_OutOfBounds_Negative() {
    Seq seq = Concat.concat(Seq.wrap("a"), Seq.wrap("b"));
    seq.charAt(-1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_OutOfBounds_Positive() {
    Seq seq = Concat.concat(Seq.wrap("a"), Seq.wrap("b"));
    seq.charAt(3);
  }

  @Test
  public void testSubSequence() {
    Seq seq = Concat.concat(Seq.wrap("abcde"), Seq.wrap("fghij"));

    // Entire sequence
    assertEquals("abcdefghij", seq.subSequence(0, 10).toString());

    // Entirely in left child
    assertEquals("bcd", seq.subSequence(1, 4).toString());

    // Entirely in right child
    assertEquals("ghi", seq.subSequence(6, 9).toString());

    // Spanning both children
    assertEquals("defg", seq.subSequence(3, 7).toString());

    // Edge cases
    assertEquals("", seq.subSequence(5, 5).toString());
    assertEquals("a", seq.subSequence(0, 1).toString());
  }

  @Test
  public void testAppendAndPrepend() {
    Seq base = Concat.concat(Seq.wrap("Base"), Seq.wrap("Seq"));
    Seq prefix = Seq.wrap("Prefix-");
    Seq suffix = Seq.wrap("-Suffix");

    Seq prepended = base.prepend(prefix);
    assertEquals("Prefix-BaseSeq", prepended.toString());
    assertEquals(14, prepended.length());

    Seq appended = base.append(suffix);
    assertEquals("BaseSeq-Suffix", appended.toString());
    assertEquals(14, appended.length());

    // Test chaining and rebalancing implicitly
    Seq chained = prefix.append(base).append(suffix);
    assertEquals("Prefix-BaseSeq-Suffix", chained.toString());
    assertEquals(21, chained.length());
  }

  @Test
  public void testHandlingEmptySequences() {
    Seq text = Seq.wrap("text");
    Seq empty = Seq.empty();

    // concat() returns the non-empty part
    assertEquals(text.toString(), Concat.concat(text, empty).toString());
    assertEquals(text.toString(), Concat.concat(empty, text).toString());

    // append/prepend should return `this`
    assertSame(text, text.append(empty));
    assertSame(text, text.prepend(empty));
  }

  // --- appendTo / appendToInternal coverage ---

  @Test
  public void testAppendTo_StringBuilder_NestedTreeProducesFullOutput() {
    // Build a multi-level tree to exercise recursive appendToInternal.
    Seq chain =
        Seq.wrap("ab").append(Seq.wrap("c")).append(Seq.wrap("def")).append(Seq.wrap("ghij"));
    assertTrue(chain instanceof Concat);

    StringBuilder sb = new StringBuilder("[");
    chain.appendTo(sb);
    sb.append("]");
    assertEquals("[abcdefghij]", sb.toString());
  }

  @Test
  public void testAppendTo_Appendable_StringBuilder() throws IOException {
    Concat seq = Concat.concat(Seq.wrap("Hello"), Seq.wrap(" World"));
    StringBuilder sb = new StringBuilder("> ");
    seq.appendTo((Appendable) sb);
    assertEquals("> Hello World", sb.toString());
  }

  @Test
  public void testAppendTo_Appendable_GenericPathProducesCorrectOutput() throws IOException {
    // For a non-StringBuilder Appendable, Concat recurses into each child's appendTo.
    // The two atomic StrView children each call append(CharSequence, start, end) once.
    Concat seq = Concat.concat(Seq.wrap("foo"), Seq.wrap("bar"));

    final StringBuilder backing = new StringBuilder();
    final AtomicInteger appendCharCount = new AtomicInteger();
    Appendable counting =
        new Appendable() {
          @Override
          public Appendable append(CharSequence csq) {
            backing.append(csq);
            return this;
          }

          @Override
          public Appendable append(CharSequence csq, int start, int end) {
            backing.append(csq, start, end);
            return this;
          }

          @Override
          public Appendable append(char c) {
            appendCharCount.incrementAndGet();
            backing.append(c);
            return this;
          }
        };

    seq.appendTo(counting);

    assertEquals("foobar", backing.toString());
    assertEquals(
        "atomic children should not fall back to per-char appends", 0, appendCharCount.get());
  }

  @Test
  public void testAppendToInternal_DirectCall_RecursesIntoChildren() {
    // Direct call must not throw and must produce the same output as appendTo.
    Concat seq = Concat.concat(Seq.wrap("left-"), Seq.wrap("right"));
    StringBuilder sb = new StringBuilder("|");
    seq.appendToInternal(sb);
    assertEquals("|left-right", sb.toString());
  }

  @Test
  public void testAppendTo_Appendable_PropagatesIOException() {
    Concat seq = Concat.concat(Seq.wrap("a"), Seq.wrap("b"));
    Appendable throwing =
        new Appendable() {
          @Override
          public Appendable append(CharSequence csq) throws IOException {
            throw new IOException("boom");
          }

          @Override
          public Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw new IOException("boom");
          }

          @Override
          public Appendable append(char c) throws IOException {
            throw new IOException("boom");
          }
        };

    try {
      seq.appendTo(throwing);
      fail("Expected IOException to propagate from the generic Appendable branch");
    } catch (IOException e) {
      assertEquals("boom", e.getMessage());
    }
  }

  @Test
  public void testIndexOf() {
    Seq seq = Concat.concat(Seq.wrap("banana"), Seq.wrap("rama"));

    assertEquals("Should find first 'a' in 'banana'", 1, seq.indexOf('a'));
    assertEquals("Should find 'b' at start", 0, seq.indexOf('b'));
    assertEquals("Should find last 'n' in 'banana'", 2, seq.indexOf('n'));
    assertEquals("Should find 'm' in 'rama'", 8, seq.indexOf('m'));
    assertEquals("Should not find 'z'", -1, seq.indexOf('z'));
  }

  @Test
  public void testUpperCase() {
    Seq seq = Concat.concat(Seq.wrap("Hello"), Seq.wrap(" World 123"));
    Seq upper = seq.upperCase();
    assertEquals("HELLO WORLD 123", upper.toString());
  }

  @Test
  public void testUpperCase_ReturnsSeqArrayNotConcat() {
    // The new override always flattens into a SeqArray (avoiding a Concat result),
    // since it builds a pre-sized AtomicSeq[] of all leaves.
    Concat seq = Concat.concat(Seq.wrap("ab"), Seq.wrap("cd"));
    Seq upper = seq.upperCase();

    assertFalse("upperCase() result should not be a Concat", upper instanceof Concat);
    assertEquals("ABCD", upper.toString());
    assertEquals(seq.length(), upper.length());
    assertEquals(seq.elementCount(), upper.elementCount());
  }

  @Test
  public void testUpperCase_RightLeaningChain() {
    // Chained appends produce a right-leaning Concat tree (after rebalancing).
    // Exercises the AtomicSeq + Concat branches of addUpperCase across multiple levels.
    Seq chain =
        Seq.wrap("Ab").append(Seq.wrap("Cd")).append(Seq.wrap("Ef")).append(Seq.wrap("Gh"));
    assertTrue(chain instanceof Concat);

    Seq upper = chain.upperCase();
    assertEquals("ABCDEFGH", upper.toString());
    assertEquals(chain.length(), upper.length());
    assertEquals(chain.elementCount(), upper.elementCount());
  }

  @Test
  public void testUpperCase_DeepChainPreservesLeafOrder() {
    // Build a long chain to make sure the recursive descent visits leaves left-to-right.
    Seq chain = Seq.wrap("a");
    for (char c = 'b'; c <= 'h'; c++) {
      chain = chain.append(Seq.wrap(String.valueOf(c)));
    }
    Seq upper = chain.upperCase();
    assertEquals("ABCDEFGH", upper.toString());
  }

  @Test
  public void testUpperCase_NestedConcatWithSeqArrayChild() {
    // A non-Concat AtomicSeqIterable child (SeqArray) exercises the third branch of
    // addUpperCase, which falls back to iterating the child's atomic leaves.
    SeqArray inner = new SeqArray(new AtomicSeq[] {Seq.wrap("cd"), Seq.wrap("ef")}, 4);
    Seq seq = Concat.concat(Seq.wrap("ab"), inner);
    assertEquals(3, seq.elementCount());

    Seq upper = seq.upperCase();
    assertEquals("ABCDEF", upper.toString());
    assertEquals(seq.length(), upper.length());
    assertEquals(seq.elementCount(), upper.elementCount());
  }

  @Test
  public void testUpperCase_MixedAtomicSubtypes() {
    // Cover every concrete AtomicSeq subtype as a Concat leaf, so each upperCase()
    // dispatch path runs through addUpperCase.
    Seq strView = Seq.wrap("foo");
    Seq repeated = Seq.repeated('x', 3);
    Seq charArr = Seq.forArray("bar".toCharArray());
    Seq lazy = Seq.lazy(sb -> sb.append("baz"), 3);
    Seq seq = Concat.concat(Concat.concat(strView, repeated), Concat.concat(charArr, lazy));

    Seq upper = seq.upperCase();
    assertEquals("FOOXXXBARBAZ", upper.toString());
    assertEquals(seq.length(), upper.length());
  }

  @Test
  public void testUpperCase_DoesNotMutateOriginal() {
    Seq seq = Concat.concat(Seq.wrap("hello"), Seq.wrap(" world"));
    String before = seq.toString();
    Seq upper = seq.upperCase();
    assertEquals("HELLO WORLD", upper.toString());
    assertEquals("Original sequence must remain unchanged", before, seq.toString());
  }

  @Test
  public void testUpperCase_Idempotent() {
    // Applying upperCase() twice must still produce the uppercase string.
    Seq seq = Concat.concat(Seq.wrap("MixedCase"), Seq.wrap("Again"));
    Seq once = seq.upperCase();
    Seq twice = once.upperCase();
    assertEquals("MIXEDCASEAGAIN", twice.toString());
  }

  @Test
  public void testUpperCase_UnknownSeqType_Throws() {
    // Defensive fallback: an unrecognised Seq subtype embedded in a Concat triggers
    // the IllegalStateException in addUpperCase.
    Seq unknown =
        new Seq() {
          @Override
          public int length() {
            return 0;
          }

          @Override
          public char charAt(int index) {
            throw new IndexOutOfBoundsException();
          }

          @Override
          public Seq subSequence(int start, int end) {
            throw new UnsupportedOperationException();
          }

          @Override
          public Seq upperCase() {
            return this;
          }

          @Override
          public int elementCount() {
            return 0;
          }

          @Override
          public boolean isAtomic() {
            return false;
          }
        };
    Concat seq = Concat.concat(unknown, Seq.wrap("a"));
    try {
      seq.upperCase();
      fail("Expected IllegalStateException for an unknown Seq subtype");
    } catch (IllegalStateException e) {
      assertTrue(
          "Message should mention the unknown type",
          e.getMessage() != null && e.getMessage().contains("Unknown Seq type"));
    }
  }

  @Test
  public void testIterator_Simple() {
    AtomicSeqIterable seq = Concat.concat(Seq.wrap("A"), Seq.wrap("B"));
    Iterator<AtomicSeq> it = seq.iterator();

    assertTrue(it.hasNext());
    assertEquals("A", it.next().toString());
    assertTrue(it.hasNext());
    assertEquals("B", it.next().toString());
    assertFalse(it.hasNext());
  }

  @Test
  public void testIterator_BalancedTree() {
    // (A + B) + (C + D)
    Seq ab = Concat.concat(Seq.wrap("A"), Seq.wrap("B"));
    Seq cd = Concat.concat(Seq.wrap("C"), Seq.wrap("D"));
    AtomicSeqIterable seq = Concat.concat(ab, cd);
    assertEquals(4, seq.elementCount());

    List<String> result =
        StreamSupport.stream(seq.spliterator(), false)
            .map(Object::toString)
            .collect(Collectors.toList());

    assertEquals(Arrays.asList("A", "B", "C", "D"), result);
  }

  @Test
  public void testIterator_LongRightLeaningChain() {
    // A + (B + (C + D)) - tests rebalancing
    Seq chain = Seq.wrap("A").append(Seq.wrap("B")).append(Seq.wrap("C")).append(Seq.wrap("D"));
    AtomicSeqIterable seq = (AtomicSeqIterable) chain;

    assertEquals(4, seq.elementCount());
    List<String> result =
        StreamSupport.stream(seq.spliterator(), false)
            .map(Object::toString)
            .collect(Collectors.toList());

    assertEquals(Arrays.asList("A", "B", "C", "D"), result);
  }

  @Test
  public void testIterator_MixedComposites() {
    // Create a SeqArray to mix in
    SeqArray arraySeq = new SeqArray(new AtomicSeq[] {Seq.wrap("C"), Seq.wrap("D")}, 2);

    // Build a tree: ("A" + "B") + SeqArray("C", "D")
    Seq ab = Concat.concat(Seq.wrap("A"), Seq.wrap("B"));
    AtomicSeqIterable seq = Concat.concat(ab, arraySeq);

    assertEquals(4, seq.elementCount());
    List<String> result = new ArrayList<>();
    Iterator<AtomicSeq> it = seq.iterator();
    while (it.hasNext()) {
      result.add(it.next().toString());
    }

    assertEquals(Arrays.asList("A", "B", "C", "D"), result);
  }

  @Test
  public void testIterator_Exhaustion() {
    AtomicSeqIterable seq = Concat.concat(Seq.wrap("A"), Seq.wrap("B"));
    Iterator<AtomicSeq> it = seq.iterator();
    it.next();
    it.next();

    // After iterating all elements, hasNext() should be false
    assertFalse(it.hasNext());

    // Calling next() again should throw an exception
    try {
      it.next();
      fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }
}
