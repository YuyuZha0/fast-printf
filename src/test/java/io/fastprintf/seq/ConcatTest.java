package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
