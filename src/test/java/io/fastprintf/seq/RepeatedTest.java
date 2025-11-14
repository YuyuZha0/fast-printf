package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class RepeatedTest {

  // --- Factory and Constructor Tests ---

  @Test
  public void testOfSingleChar_usesCacheForAscii() {
    for (char c = 0; c < 128; c++) {
      Repeated r1 = Repeated.ofSingleChar(c);
      Repeated r2 = Repeated.ofSingleChar(c);
      assertSame("Expected cached instance for ASCII char: " + (int) c, r1, r2);
    }
  }

  @Test
  public void testOfSingleChar_createsNewForNonAscii() {
    char nonAscii = '€';
    Repeated r1 = Repeated.ofSingleChar(nonAscii);
    Repeated r2 = Repeated.ofSingleChar(nonAscii);
    assertNotSame("Expected new instance for non-ASCII char", r1, r2);
    assertEquals(r1.toString(), r2.toString());
  }

  @Test
  public void testConstructor_createsMultiCharInstance() {
    Repeated r = new Repeated('x', 10);
    assertEquals(10, r.length());
    assertEquals('x', r.charAt(5));
  }

  // --- Core Method Tests ---

  @Test
  public void testLengthAndIsEmpty() {
    assertEquals(5, new Repeated('a', 5).length());
    assertFalse(new Repeated('a', 5).isEmpty());
    assertEquals(1, Repeated.ofSingleChar('a').length());
    assertEquals(0, new Repeated('a', 0).length());
    assertTrue(new Repeated('a', 0).isEmpty());
  }

  @Test
  public void testCharAt() {
    Repeated r = new Repeated('*', 20);
    assertEquals('*', r.charAt(0));
    assertEquals('*', r.charAt(10));
    assertEquals('*', r.charAt(19));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsForNegativeIndex() {
    new Repeated('a', 5).charAt(-1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsForIndexTooLarge() {
    new Repeated('a', 5).charAt(5);
  }

  @Test
  public void testIndexOf() {
    Repeated r = new Repeated('b', 10);
    assertEquals("Should find the char at index 0", 0, r.indexOf('b'));
    assertEquals("Should not find a different char", -1, r.indexOf('x'));
  }

  @Test
  public void testIndexOf_onEmptyRepeated() {
    Repeated r = new Repeated('b', 0);
    assertEquals(-1, r.indexOf('b'));
  }

  // --- Transformation Tests ---

  @Test
  public void testSubSequence() {
    Repeated r = new Repeated('z', 10);
    AtomicSeq sub = r.subSequence(2, 7);
    assertTrue(sub instanceof Repeated);
    assertEquals(5, sub.length());
    assertEquals('z', sub.charAt(0));
    assertEquals("zzzzz", sub.toString());
  }

  @Test
  public void testSubSequence_returnsEmptySeq() {
    Repeated r = new Repeated('z', 10);
    assertSame(Seq.empty(), r.subSequence(5, 5));
  }

  @Test
  public void testUpperCase() {
    Repeated lower = new Repeated('a', 5);
    Repeated upper = lower.upperCase();

    assertNotSame(lower, upper);
    assertEquals('A', upper.charAt(0));
    assertEquals(5, upper.length());
    assertEquals("AAAAA", upper.toString());
  }

  @Test
  public void testUpperCase_isIdempotent() {
    Repeated upper1 = new Repeated('a', 5).upperCase();
    Repeated upper2 = upper1.upperCase();
    assertSame("Calling upperCase on an uppercase char should return itself", upper1, upper2);
  }

  @Test
  public void testUpperCase_onNonLowerCase() {
    Repeated r = new Repeated('B', 3);
    assertSame("Calling upperCase on a non-lowercase char should return itself", r, r.upperCase());

    Repeated r2 = new Repeated('7', 3);
    assertSame("Calling upperCase on a digit should return itself", r2, r2.upperCase());
  }

  // --- Output Method Tests ---

  @Test
  public void testToString() {
    assertEquals("c", Repeated.ofSingleChar('c').toString());
    assertEquals("*****", new Repeated('*', 5).toString());
    assertEquals("", new Repeated('*', 0).toString());
  }

  @Test
  public void testAppendTo_StringBuilder_LoopPath() {
    StringBuilder sb = new StringBuilder();
    // Use a count less than the threshold
    new Repeated('x', 5).appendTo(sb);
    assertEquals("xxxxx", sb.toString());
  }

  @Test
  public void testAppendTo_StringBuilder_ArrayPath() {
    StringBuilder sb = new StringBuilder();
    // Use a count greater than or equal to the threshold (16)
    new Repeated('y', 20).appendTo(sb);
    assertEquals("yyyyyyyyyyyyyyyyyyyy", sb.toString());
  }

  @Test
  public void testAppendTo_Appendable() throws IOException {
    StringBuilder sb = new StringBuilder();
    new Repeated('z', 10).appendTo((Appendable) sb);
    assertEquals("zzzzzzzzzz", sb.toString());
  }

  @Test
  public void testAppendTo_empty() {
    StringBuilder sb = new StringBuilder("start-");
    new Repeated('a', 0).appendTo(sb);
    assertEquals("start-", sb.toString());
  }

  /**
   * Directly tests the public static helper method `appendRepeated`. This ensures the fallback
   * logic used on older JDKs is correct and can be tested in isolation.
   */
  @Test
  public void testAppendRepeated_staticHelper() {
    // Test loop path
    StringBuilder sb1 = new StringBuilder();
    Repeated.appendRepeated(sb1, new Repeated('a', 5));
    assertEquals("aaaaa", sb1.toString());

    // Test array path
    StringBuilder sb2 = new StringBuilder();
    Repeated.appendRepeated(sb2, new Repeated('b', 20));
    assertEquals("bbbbbbbbbbbbbbbbbbbb", sb2.toString());
  }
}
