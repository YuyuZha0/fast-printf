package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SeqTest {

  // --- Static Factory Method Tests ---

  @Test
  public void testCh() {
    Seq s = Seq.ch('a');
    assertEquals(1, s.length());
    assertEquals("a", s.toString());
    assertTrue(s instanceof Repeated);
  }

  @Test
  public void testRepeated() {
    Seq s = Seq.repeated('x', 5);
    assertEquals(5, s.length());
    assertEquals("xxxxx", s.toString());
    assertTrue(s instanceof Repeated);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRepeated_ZeroCount() {
    Seq.repeated('x', 0);
  }

  @Test
  public void testWrap_String() {
    assertEquals("hello", Seq.wrap("hello").toString());
    assertEquals(5, Seq.wrap("hello").length());
    assertTrue(Seq.wrap("hello") instanceof StrView);

    // Optimization check: single char should return a Repeated instance
    assertTrue(Seq.wrap("a") instanceof Repeated);

    // Empty string
    assertSame(EmptySeq.INSTANCE, Seq.wrap(""));
  }

  @Test
  public void testWrap_StringWithOffsets() {
    assertEquals("llo", Seq.wrap("hello", 2).toString());
    assertEquals("lo", Seq.wrap("hello", 3, 5).toString());
    assertSame(EmptySeq.INSTANCE, Seq.wrap("hello", 2, 2));

    // Optimization check: single char substring
    assertTrue(Seq.wrap("hello", 1, 2) instanceof Repeated);
  }

  @Test
  public void testForArray() {
    char[] arr = {'a', 'b', 'c', 'd'};
    assertEquals("abcd", Seq.forArray(arr).toString());
    assertEquals("bc", Seq.forArray(arr, 1, 2).toString());
    assertTrue(Seq.forArray(arr) instanceof CharArray);
  }

  @Test
  public void testConcat() {
    Seq s1 = Seq.wrap("hello");
    Seq s2 = Seq.wrap(" world");
    Seq result = Seq.concat(s1, s2);
    assertEquals("hello world", result.toString());
    assertTrue(result instanceof Concat);
  }

  @Test
  public void testEmpty() {
    Seq s = Seq.empty();
    assertEquals(0, s.length());
    assertEquals("", s.toString());
    assertSame(EmptySeq.INSTANCE, s);
  }

  @Test
  public void testJoin() {
    List<AtomicSeq> parts = Arrays.asList(Seq.wrap("A"), Seq.wrap("B"), Seq.wrap("C"));
    Seq result = Seq.join(parts);
    assertEquals("ABC", result.toString());
    assertTrue(result instanceof SeqArray);

    // Edge cases
    assertSame(EmptySeq.INSTANCE, Seq.join(Collections.emptyList()));
    AtomicSeq single = Seq.wrap("single");
    assertSame(single, Seq.join(Collections.singletonList(single)));
  }

  // --- Default Method Tests ---
  // Using a known implementation (StrView) to test default methods.
  @Test
  public void testPrependAndAppend() {
    Seq base = Seq.wrap("base");
    assertEquals("PREFIXbase", base.prepend(Seq.wrap("PREFIX")).toString());
    assertEquals("baseSUFFIX", base.append(Seq.wrap("SUFFIX")).toString());
    assertSame(base, base.append(Seq.empty())); // Should return self
    assertSame(base, base.prepend(Seq.empty()));
  }

  @Test
  public void testAppendTo() throws IOException {
    Seq s = Seq.concat(Seq.wrap("one"), Seq.wrap("two"));

    StringBuilder sb = new StringBuilder();
    s.appendTo(sb);
    assertEquals("onetwo", sb.toString());

    // Test with a generic Appendable
    StringBuilder sb2 = new StringBuilder();
    s.appendTo((Appendable) sb2);
    assertEquals("onetwo", sb2.toString());
  }

  @Test
  public void testDup() {
    Seq s = Seq.wrap("test");
    assertSame(s, s.dup());
  }

  @Test
  public void testMap() {
    Seq s = Seq.wrap("test");
    Seq result = s.map(seq -> Seq.wrap("mapped"));
    assertEquals("mapped", result.toString());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(Seq.empty().isEmpty());
    assertFalse(Seq.wrap("a").isEmpty());
    assertFalse(Seq.repeated('x', 10).isEmpty());
  }

  @Test
  public void testIndexOf() {
    Seq s = Seq.concat(Seq.wrap("ab"), Seq.wrap("ca"));
    assertEquals(0, s.indexOf('a'));
    assertEquals(1, s.indexOf('b'));
    assertEquals(2, s.indexOf('c'));
    assertEquals(Seq.INDEX_NOT_FOUND, s.indexOf('z'));
  }

  @Test
  public void testStartsWith() {
    Seq s = Seq.wrap("test");
    assertTrue(s.startsWith('t'));
    assertFalse(s.startsWith('e'));
    assertFalse(Seq.empty().startsWith('x'));
  }
}
