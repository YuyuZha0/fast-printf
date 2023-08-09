package org.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StrViewTest {

  @Test
  public void test1() {
    Seq seq = Seq.wrap("abc");
    assertEquals(3, seq.length());
    assertEquals("abc", seq.toString());
    assertEquals('a', seq.charAt(0));
    assertEquals('b', seq.charAt(1));
    assertEquals('c', seq.charAt(2));
    assertEquals(-1, seq.indexOf('d'));
    assertEquals(0, seq.indexOf('a'));
    assertEquals(1, seq.indexOf('b'));
    assertEquals(2, seq.indexOf('c'));
    assertEquals(1, seq.subSequence(0, 1).length());
    assertEquals(2, seq.subSequence(0, 2).length());
    assertEquals(3, seq.subSequence(0, 3).length());
    assertEquals(0, seq.subSequence(1, 1).length());
    assertEquals(1, seq.subSequence(1, 2).length());
    assertEquals(1, seq.subSequence(2, 3).length());
    assertEquals(0, seq.subSequence(3, 3).length());
    assertEquals("a", seq.subSequence(0, 1).toString());
    assertEquals("ab", seq.subSequence(0, 2).toString());
    assertEquals("abc", seq.subSequence(0, 3).toString());

    StringBuilder builder = new StringBuilder();
    seq.appendTo(builder);
    assertEquals("abc", builder.toString());

    assertEquals("ABC", seq.upperCase().toString());
  }

  @Test
  public void test2() {
    Seq seq = Seq.wrap("1234pabc", 5);
    assertEquals(3, seq.length());
    assertEquals("abc", seq.toString());
    assertEquals('a', seq.charAt(0));
    assertEquals('b', seq.charAt(1));
    assertEquals('c', seq.charAt(2));
    assertEquals(-1, seq.indexOf('d'));
    assertEquals(0, seq.indexOf('a'));
    assertEquals(1, seq.indexOf('b'));
    assertEquals(2, seq.indexOf('c'));
    assertEquals(1, seq.subSequence(0, 1).length());
    assertEquals(2, seq.subSequence(0, 2).length());
    assertEquals(3, seq.subSequence(0, 3).length());
    assertEquals(0, seq.subSequence(1, 1).length());
    assertEquals(1, seq.subSequence(1, 2).length());
    assertEquals(1, seq.subSequence(2, 3).length());
    assertEquals(0, seq.subSequence(3, 3).length());
    assertEquals("a", seq.subSequence(0, 1).toString());
    assertEquals("ab", seq.subSequence(0, 2).toString());
    assertEquals("abc", seq.subSequence(0, 3).toString());

    StringBuilder builder = new StringBuilder();
    seq.appendTo(builder);
    assertEquals("abc", builder.toString());

    assertEquals("ABC", seq.upperCase().toString());
  }

  @Test
  public void test3() {
    Seq seq = Seq.wrap("1234pabcIU@&", 5, 8);
    assertEquals(3, seq.length());
    assertEquals("abc", seq.toString());
    assertEquals('a', seq.charAt(0));
    assertEquals('b', seq.charAt(1));
    assertEquals('c', seq.charAt(2));
    assertEquals(-1, seq.indexOf('d'));
    assertEquals(0, seq.indexOf('a'));
    assertEquals(1, seq.indexOf('b'));
    assertEquals(2, seq.indexOf('c'));
    assertEquals(1, seq.subSequence(0, 1).length());
    assertEquals(2, seq.subSequence(0, 2).length());
    assertEquals(3, seq.subSequence(0, 3).length());
    assertEquals(0, seq.subSequence(1, 1).length());
    assertEquals(1, seq.subSequence(1, 2).length());
    assertEquals(1, seq.subSequence(2, 3).length());
    assertEquals(0, seq.subSequence(3, 3).length());
    assertEquals("a", seq.subSequence(0, 1).toString());
    assertEquals("ab", seq.subSequence(0, 2).toString());
    assertEquals("abc", seq.subSequence(0, 3).toString());

    StringBuilder builder = new StringBuilder();
    seq.appendTo(builder);
    assertEquals("abc", builder.toString());

    assertEquals("ABC", seq.upperCase().toString());
  }
}
