package io.fastprintf.seq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import io.fastprintf.util.Utils;
import java.io.IOException;
import java.util.Locale;
import org.junit.Test;

public class StrViewTest {

  private static final String TEST_STRING = "Hello World!";
  private static final String LONG_STRING =
      "This is a string that is longer than sixteen characters.";

  // --- Factory and Construction Tests ---
  private static final String INDEX_TEST_STRING = "Test 123! Mixed-Case";

  @Test
  public void testWrap_createsCorrectView() {
    // Wrap a subsection of the string
    AtomicSeq seq = Seq.wrap(TEST_STRING, 6, 11); // "World"
    assertEquals(5, seq.length());
    assertEquals("World", seq.toString());
  }

  // --- Core CharSequence Method Tests ---

  @Test
  public void testWrap_fullString() {
    AtomicSeq seq = Seq.wrap(TEST_STRING);
    assertEquals(TEST_STRING.length(), seq.length());
    assertEquals(TEST_STRING, seq.toString());
  }

  @Test
  public void testLength() {
    assertEquals(12, Seq.wrap(TEST_STRING).length());
    assertEquals(5, Seq.wrap(TEST_STRING, 6, 11).length());
    assertEquals(0, Seq.wrap(TEST_STRING, 5, 5).length());
  }

  @Test
  public void testCharAt() {
    AtomicSeq seq = Seq.wrap(TEST_STRING);
    assertEquals('H', seq.charAt(0));
    assertEquals(' ', seq.charAt(5));
    assertEquals('!', seq.charAt(11));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsExceptionForNegativeIndex() {
    Seq.wrap(TEST_STRING, 0, 5).charAt(-1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testCharAt_throwsExceptionForIndexTooLarge() {
    Seq.wrap(TEST_STRING, 0, 5).charAt(5);
  }

  @Test
  public void testSubSequence() {
    AtomicSeq seq = Seq.wrap(TEST_STRING);
    AtomicSeq sub = seq.subSequence(1, 5); // "ello"
    assertEquals(4, sub.length());
    assertEquals("ello", sub.toString());
    assertEquals('e', sub.charAt(0));
    assertTrue(seq.subSequence(1, 1).isEmpty());
  }

  // --- Lazy upperCase Functionality Tests ---

  @Test
  public void testSubSequence_ofSubSequence() {
    AtomicSeq seq = Seq.wrap(TEST_STRING, 0, 11); // "Hello World"
    AtomicSeq sub1 = seq.subSequence(6, 11); // "World"
    AtomicSeq sub2 = sub1.subSequence(1, 4); // "orl"
    assertEquals("orl", sub2.toString());
  }

  @Test
  public void testUpperCase_isLazyAndImmutable() {
    AtomicSeq original = Seq.wrap(TEST_STRING);
    AtomicSeq uppercased = original.upperCase();

    // Should return a new instance, not modify the original
    assertNotSame(original, uppercased);
    assertTrue(uppercased instanceof StrView);

    // Original should be unchanged
    assertEquals("Hello World!", original.toString());

    // New one should be uppercased
    assertEquals("HELLO WORLD!", uppercased.toString());
  }

  @Test
  public void testUpperCase_isIdempotent() {
    AtomicSeq seq = Seq.wrap(TEST_STRING);
    AtomicSeq upper1 = seq.upperCase();
    AtomicSeq upper2 = upper1.upperCase();

    // Calling upperCase() on an already-uppercased view should return itself
    assertSame(upper1, upper2);
  }

  @Test
  public void testCharAt_onUpperCase() {
    AtomicSeq seq = Seq.wrap(TEST_STRING).upperCase();
    assertEquals('H', seq.charAt(0));
    assertEquals('E', seq.charAt(1));
    assertEquals(' ', seq.charAt(5));
    assertEquals('!', seq.charAt(11));
  }

  // --- Utility Method Tests ---

  @Test
  public void testSubSequence_propagatesUpperCaseState() {
    AtomicSeq seq = Seq.wrap(TEST_STRING).upperCase();
    AtomicSeq sub = seq.subSequence(1, 5); // Should be "ELLO"

    assertEquals("ELLO", sub.toString());
    assertEquals('E', sub.charAt(0));

    // Ensure the original is not affected
    assertEquals("HELLO WORLD!", seq.toString());
  }

  @Test
  public void testAppendTo_StringBuilder() {
    StringBuilder sb = new StringBuilder();
    Seq.wrap(TEST_STRING, 1, 5).appendTo(sb); // "ello"
    assertEquals("ello", sb.toString());
  }

  @Test
  public void testAppendTo_StringBuilderOnUpperCase() {
    StringBuilder sb = new StringBuilder();
    Seq.wrap(TEST_STRING, 1, 5).upperCase().appendTo(sb); // "ELLO"
    assertEquals("ELLO", sb.toString());
  }

  @Test
  public void testAppendTo_StringBuilderHeuristics() {
    // Test short string path (< 16)
    StringBuilder sb1 = new StringBuilder();
    Seq.wrap(LONG_STRING, 0, 10).appendTo(sb1);
    assertEquals("This is a ", sb1.toString());

    // Test long string path (>= 16), which uses toCharArray()
    StringBuilder sb2 = new StringBuilder();
    Seq.wrap(LONG_STRING, 0, 20).appendTo(sb2);
    assertEquals("This is a string tha", sb2.toString());

    // Test full string optimization
    StringBuilder sb3 = new StringBuilder();
    Seq.wrap(LONG_STRING).appendTo(sb3);
    assertEquals(LONG_STRING, sb3.toString());
  }

  @Test
  public void testAppendTo_Appendable() throws IOException {
    StringBuffer sb = new StringBuffer(); // Use a different Appendable
    Seq.wrap(TEST_STRING, 6, 11).appendTo(sb); // "World"
    assertEquals("World", sb.toString());
  }

  @Test
  public void testAppendTo_AppendableOnUpperCase() throws IOException {
    StringBuffer sb = new StringBuffer();
    Seq.wrap(TEST_STRING, 6, 11).upperCase().appendTo(sb); // "WORLD"
    assertEquals("WORLD", sb.toString());
  }

  @Test
  public void testIndexOf() {
    AtomicSeq seq = Seq.wrap(TEST_STRING);
    assertEquals(0, seq.indexOf('H'));
    assertEquals(2, seq.indexOf('l'));
    assertEquals(11, seq.indexOf('!'));
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('z'));
  }

  @Test
  public void testIndexOf_onUpperCase() {
    AtomicSeq seq = Seq.wrap(TEST_STRING).upperCase();
    assertEquals(0, seq.indexOf('H'));
    assertEquals(2, seq.indexOf('L')); // Should find uppercase L
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('l')); // Should NOT find lowercase l
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('z'));
  }

  // --- Focused Tests for the Optimized indexOf Method ---

  @Test
  public void testIsEmpty() {
    assertTrue(Seq.wrap(TEST_STRING, 5, 5).isEmpty());
    assertFalse(Seq.wrap(TEST_STRING).isEmpty());
  }

  /** Verifies Path 4: The standard, non-uppercased view correctly uses the fast path. */
  @Test
  public void indexOf_onNormalView_usesFastPathCorrectly() {
    // Arrange
    AtomicSeq seq = Seq.wrap(INDEX_TEST_STRING);

    // Act & Assert
    assertEquals(INDEX_TEST_STRING.indexOf('T'), seq.indexOf('T')); // Find uppercase
    assertEquals(INDEX_TEST_STRING.indexOf('e'), seq.indexOf('e')); // Find lowercase
    assertEquals(INDEX_TEST_STRING.indexOf('1'), seq.indexOf('1')); // Find digit
    assertEquals(INDEX_TEST_STRING.indexOf('!'), seq.indexOf('!')); // Find symbol
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('Z')); // Not found
  }

  /**
   * Verifies Path 1: The fast-exit path when searching for a lowercase character in a conceptually
   * uppercase view.
   */
  @Test
  public void indexOf_onUpperCaseView_returnsNotFoundForLowerCaseChar() {
    // Arrange
    AtomicSeq seq = Seq.wrap(INDEX_TEST_STRING).upperCase();

    // Act
    int result = seq.indexOf('e');

    // Assert
    assertEquals("Should not find lowercase 'e' in an uppercase view", Seq.INDEX_NOT_FOUND, result);
  }

  /**
   * Verifies Path 2: The optimized path where non-alphabetic characters are found using the fast
   * underlying String.indexOf.
   */
  @Test
  public void indexOf_onUpperCaseView_findsNonAlphabeticCharUsingFastPath() {
    // Arrange
    AtomicSeq seq = Seq.wrap(INDEX_TEST_STRING).upperCase();
    String expectedUpper = Utils.toUpperCase(INDEX_TEST_STRING);

    // Act & Assert
    assertEquals("Should find space", expectedUpper.indexOf(' '), seq.indexOf(' '));
    assertEquals("Should find digit '2'", expectedUpper.indexOf('2'), seq.indexOf('2'));
    assertEquals("Should find symbol '!'", expectedUpper.indexOf('!'), seq.indexOf('!'));
    assertEquals("Should find symbol '-'", expectedUpper.indexOf('-'), seq.indexOf('-'));
    assertEquals("Should not find missing symbol", Seq.INDEX_NOT_FOUND, seq.indexOf('@'));
  }

  /**
   * Verifies Path 3: The fallback path that performs a character-by-character conversion loop when
   * searching for an uppercase letter.
   */
  @Test
  public void indexOf_onUpperCaseView_findsUpperCaseCharUsingConvertingLoop() {
    // Arrange
    // "Test 123! Mixed-Case" becomes "TEST 123! MIXED-CASE"
    AtomicSeq seq = Seq.wrap(INDEX_TEST_STRING).upperCase();
    String expectedUpper = INDEX_TEST_STRING.toUpperCase(Locale.US);

    // Act & Assert

    // Find 'E', which is lowercase 'e' at index 1 in the original string
    assertEquals("Should find 'E' from original 'e'", expectedUpper.indexOf('E'), seq.indexOf('E'));

    // Find 'T', which is uppercase 'T' at index 0 in the original string
    assertEquals("Should find 'T' from original 'T'", expectedUpper.indexOf('T'), seq.indexOf('T'));

    // Find 'M', which is uppercase 'M' at index 11 in the original string
    assertEquals("Should find 'M' from original 'M'", expectedUpper.indexOf('M'), seq.indexOf('M'));

    // Find 'C', which is lowercase 'c' at index 17 in the original string
    assertEquals("Should find 'C' from original 'c'", expectedUpper.indexOf('C'), seq.indexOf('C'));

    // Ensure it finds the *first* occurrence correctly
    // The original string is "Test...Mixed-Case". Uppercased is "TEST...MIXED-CASE".
    // It should find the 'S' from "Test" at index 2, not the one from "Case".
    assertEquals("Should find first 'S' in 'Test'", expectedUpper.indexOf('S'), seq.indexOf('S'));

    // Search for a character not present in the string
    assertEquals("Should not find 'Z'", Seq.INDEX_NOT_FOUND, seq.indexOf('Z'));
  }

  /** Verifies correct behavior on a view that is a subsection of the original string. */
  @Test
  public void indexOf_onUpperCaseSubSequenceView() {
    // Arrange
    // View on "Mixed-Case" which becomes "MIXED-CASE"
    AtomicSeq seq = Seq.wrap(INDEX_TEST_STRING, 10, INDEX_TEST_STRING.length()).upperCase();
    assertEquals("MIXED-CASE", seq.toString());

    // Act & Assert
    assertEquals(0, seq.indexOf('M')); // 'M' is at index 0 of the subsequence
    assertEquals(6, seq.indexOf('C')); // 'C' is at index 6 of the subsequence
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('T')); // 'T' is not in this subsequence
  }
}
