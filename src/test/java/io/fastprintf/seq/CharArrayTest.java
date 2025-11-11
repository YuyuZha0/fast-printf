package io.fastprintf.seq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.Test;

public class CharArrayTest {

  private final char[] TEST_CHARS = "Hello World".toCharArray();

  // --- Factory Method Tests ---

  @Test
  public void testWrap_createsCorrectView() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 6, 5); // "World"
    assertEquals(5, seq.length());
    assertEquals("World", seq.toString());
  }

  @Test
  public void testWrap_isZeroCopyAndSharesBackingArray() {
    char[] originalData = {'a', 'b', 'c'};
    AtomicSeq seq = CharArray.wrap(originalData, 0, 3);
    assertEquals("abc", seq.toString());
    originalData[1] = 'X';
    assertEquals("aXc", seq.toString());
  }

  @Test
  public void testCreate_createsDefensiveCopy() {
    char[] originalData = {'a', 'b', 'c'};
    AtomicSeq seq = CharArray.create(originalData, 0, 3);
    assertEquals("abc", seq.toString());
    originalData[1] = 'X';
    assertEquals("abc", seq.toString());
  }

  // --- Core Method Tests ---

  @Test
  public void testLength() {
    assertEquals(11, CharArray.wrap(TEST_CHARS, 0, 11).length());
    assertEquals(5, CharArray.wrap(TEST_CHARS, 6, 5).length());
    assertEquals(0, CharArray.wrap(TEST_CHARS, 5, 0).length());
  }

  @Test
  public void testCharAt() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11);
    assertEquals('H', seq.charAt(0));
    assertEquals(' ', seq.charAt(5));
    assertEquals('d', seq.charAt(10));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsExceptionForNegativeIndex() {
    CharArray.wrap(TEST_CHARS, 0, 5).charAt(-1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsExceptionForIndexTooLarge() {
    CharArray.wrap(TEST_CHARS, 0, 5).charAt(5);
  }

  // --- subSequence Tests ---

  @Test
  public void testSubSequence_createsCorrectView() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11);
    AtomicSeq sub = seq.subSequence(1, 5); // "ello"
    assertEquals(4, sub.length());
    assertEquals("ello", sub.toString());
    assertEquals('e', sub.charAt(0));
    assertTrue("Should return CharArray for multi-char subsequence", sub instanceof CharArray);
  }

  @Test
  public void testSubSequence_retainsUpperCaseState() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11).upperCase();
    AtomicSeq sub = seq.subSequence(1, 5); // Should be "ELLO"
    assertEquals("ELLO", sub.toString());
    assertEquals('E', sub.charAt(0));
    // Ensure the original is not affected
    assertEquals("HELLO WORLD", seq.toString());
  }

  @Test
  public void testSubSequence_returnsRepeatedForSingleChar() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11); // "Hello World"
    AtomicSeq sub = seq.subSequence(1, 2); // "e"

    assertTrue("Single-char subsequence should be a Repeated instance", sub instanceof Repeated);
    assertEquals(1, sub.length());
    assertEquals('e', sub.charAt(0));
    assertEquals("e", sub.toString());
  }

  @Test
  public void testSubSequence_returnsRepeatedForSingleCharFromUpperCaseView() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11).upperCase(); // "HELLO WORLD"
    AtomicSeq sub = seq.subSequence(1, 2); // "E"

    assertTrue("Single-char subsequence should be a Repeated instance", sub instanceof Repeated);
    assertEquals(1, sub.length());
    assertEquals('E', sub.charAt(0));
    assertEquals("E", sub.toString());
  }

  @Test
  public void testSubSequence_returnsEmptySeqForZeroLength() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11);
    AtomicSeq sub = seq.subSequence(5, 5);
    assertSame(Seq.empty(), sub);
    assertEquals(0, sub.length());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSubSequence_throwsForStartGreaterThanEnd() {
    CharArray.wrap(TEST_CHARS, 0, 11).subSequence(5, 4);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSubSequence_throwsForNegativeStart() {
    CharArray.wrap(TEST_CHARS, 0, 11).subSequence(-1, 4);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSubSequence_throwsForEndTooLarge() {
    CharArray.wrap(TEST_CHARS, 0, 11).subSequence(0, 12);
  }

  // --- Transformation Tests ---

  @Test
  public void testUpperCase_isLazyAndImmutable() {
    AtomicSeq original = CharArray.wrap(TEST_CHARS, 0, 11);
    AtomicSeq uppercased = original.upperCase();
    assertNotSame(original, uppercased);
    assertEquals("Hello World", original.toString());
    assertEquals("HELLO WORLD", uppercased.toString());
  }

  @Test
  public void testUpperCase_isIdempotent() {
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 0, 11);
    AtomicSeq upper1 = seq.upperCase();
    AtomicSeq upper2 = upper1.upperCase();
    assertSame(upper1, upper2);
  }

  // --- indexOf Tests ---

  @Test
  public void indexOf_onNormalView() {
    AtomicSeq seq = CharArray.wrap("abracadabra".toCharArray(), 0, 11);
    assertEquals(0, seq.indexOf('a'));
    assertEquals(1, seq.indexOf('b'));
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('z'));
    assertEquals(4, seq.indexOf('c'));
  }

  @Test
  public void indexOf_findsFirstOccurrence() {
    AtomicSeq seq = CharArray.wrap("abracadabra".toCharArray(), 0, 11);
    assertEquals(0, seq.indexOf('a')); // Not 3, 5, 7, or 10
    assertEquals(1, seq.indexOf('b')); // Not 8
  }

  @Test
  public void indexOf_onUpperCaseView_findsCorrectChars() {
    AtomicSeq seq = CharArray.wrap("abracadabra".toCharArray(), 0, 11).upperCase();
    assertEquals(0, seq.indexOf('A'));
    assertEquals(1, seq.indexOf('B'));
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('a')); // Should not find lowercase
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('z'));
  }

  @Test
  public void indexOf_onUpperCaseView_withMixedContent() {
    AtomicSeq seq = CharArray.wrap("Test 123!".toCharArray(), 0, 9).upperCase(); // "TEST 123!"
    assertEquals(1, seq.indexOf('E'));
    assertEquals(5, seq.indexOf('1'));
    assertEquals(8, seq.indexOf('!'));
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('e'));
  }

  @Test
  public void indexOf_onSubSequence() {
    AtomicSeq seq = CharArray.wrap("abracadabra".toCharArray(), 0, 11);
    AtomicSeq sub = seq.subSequence(4, 8); // "cada"
    assertEquals(1, sub.indexOf('a'));
    assertEquals(0, sub.indexOf('c'));
    assertEquals(Seq.INDEX_NOT_FOUND, sub.indexOf('b'));
  }

  @Test
  public void indexOf_onUpperCaseSubSequence() {
    AtomicSeq seq = CharArray.wrap("abracadabra".toCharArray(), 0, 11);
    AtomicSeq sub = seq.subSequence(4, 8).upperCase(); // "CADA"
    assertEquals(1, sub.indexOf('A'));
    assertEquals(Seq.INDEX_NOT_FOUND, sub.indexOf('a'));
    assertEquals(0, sub.indexOf('C'));
    assertEquals(Seq.INDEX_NOT_FOUND, sub.indexOf('B'));
  }

  // --- Utility Method Tests ---

  @Test
  public void testAppendTo() throws IOException {
    StringBuilder sb = new StringBuilder();
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 1, 4); // "ello"
    seq.appendTo(sb);
    assertEquals("ello", sb.toString());
  }

  @Test
  public void testAppendTo_onUpperCase() throws IOException {
    StringBuilder sb = new StringBuilder();
    AtomicSeq seq = CharArray.wrap(TEST_CHARS, 1, 4).upperCase(); // "ELLO"
    seq.appendTo(sb);
    assertEquals("ELLO", sb.toString());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(CharArray.wrap(TEST_CHARS, 5, 0).isEmpty());
    assertFalse(CharArray.wrap(TEST_CHARS, 0, 11).isEmpty());
  }
}
