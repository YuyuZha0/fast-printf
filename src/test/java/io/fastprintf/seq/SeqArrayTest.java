package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

public class SeqArrayTest {

  private AtomicSeq partA;
  private AtomicSeq partB;
  private AtomicSeq partC;
  private SeqArray seqArray;

  @Before
  public void setUp() {
    partA = Seq.wrap("A");
    partB = Seq.wrap("BC");
    partC = Seq.wrap("def");
    seqArray = new SeqArray(new AtomicSeq[] {partA, partB, partC}, 6);
  }

  @Test
  public void testLengthAndToString() {
    assertEquals(6, seqArray.length());
    assertEquals("ABCdef", seqArray.toString());
  }

  @Test
  public void testUpperCase() {
    Seq upper = seqArray.upperCase();
    assertNotSame("upperCase should create a new instance", seqArray, upper);
    assertEquals("ABCDEF", upper.toString());
    assertEquals(6, upper.length());
  }

  @Test
  public void testIterator_HappyPath() {
    Iterator<AtomicSeq> it = seqArray.iterator();
    assertTrue(it.hasNext());
    assertSame(partA, it.next());
    assertTrue(it.hasNext());
    assertSame(partB, it.next());
    assertTrue(it.hasNext());
    assertSame(partC, it.next());
    assertFalse(it.hasNext());
  }

  @Test(expected = NoSuchElementException.class)
  public void testIterator_Exhaustion() {
    Iterator<AtomicSeq> it = seqArray.iterator();
    it.next(); // A
    it.next(); // BC
    it.next(); // def
    it.next(); // Should throw
  }

  @Test
  public void testUnfold_CorrectlyPopulatesStackAndReturnsFirstChild() {
    Deque<Seq> stack = new ArrayDeque<>();
    Seq firstChild = seqArray.unfold(stack);

    // The first child should be returned directly
    assertSame("unfold should return the first element", partA, firstChild);

    // The stack should contain the rest of the children in reverse order
    assertEquals("Stack should contain the remaining 2 elements", 2, stack.size());
    assertSame("Second element should be at the top of the stack", partB, stack.pop());
    assertSame("First element should be at the bottom", partC, stack.pop());
    assertTrue("Stack should be empty after popping all elements", stack.isEmpty());
  }

  @Test
  public void testUnfold_WithSingleElementArray() {
    SeqArray singleElementArray = new SeqArray(new AtomicSeq[] {partA}, 1);
    Deque<Seq> stack = new ArrayDeque<>();
    Seq firstChild = singleElementArray.unfold(stack);

    assertSame("Should return the only element", partA, firstChild);
    assertTrue("Stack should remain empty", stack.isEmpty());
  }

  @Test
  public void testDefaultMethod_charAt() {
    // Within first part
    assertEquals('A', seqArray.charAt(0));
    // Boundary between A and BC
    assertEquals('B', seqArray.charAt(1));
    assertEquals('C', seqArray.charAt(2));
    // Boundary between BC and def
    assertEquals('d', seqArray.charAt(3));
    // Within last part
    assertEquals('f', seqArray.charAt(5));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testDefaultMethod_charAt_OutOfBounds() {
    seqArray.charAt(6);
  }

  @Test
  public void testDefaultMethod_subSequence() {
    // Subsequence fully within one part
    assertEquals("BC", seqArray.subSequence(1, 3).toString());
    // Subsequence spanning multiple parts
    assertEquals("Cde", seqArray.subSequence(2, 5).toString());
    // Subsequence from start to middle
    assertEquals("ABC", seqArray.subSequence(0, 3).toString());
    // Subsequence from middle to end
    assertEquals("def", seqArray.subSequence(3, 6).toString());
  }

  @Test
  public void testDefaultMethod_indexOf() {
    assertEquals(0, seqArray.indexOf('A'));
    assertEquals(2, seqArray.indexOf('C'));
    assertEquals(4, seqArray.indexOf('e'));
    assertEquals(-1, seqArray.indexOf('z'));
  }
}
