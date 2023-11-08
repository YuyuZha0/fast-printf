package io.fastprintf.seq;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CharArrayTest {
  @Test
  public void testHashCode() {
    CharArray ca = new CharArray("abc".toCharArray(), 0, 3, false);
    Assert.assertEquals("abc".hashCode(), ca.hashCode());
  }

  @Test
  public void testLength() {
    CharArray ca = new CharArray("abc".toCharArray(), 0, 3, true);
    Assert.assertEquals(3, ca.length());
  }

  @Test
  public void testCharAt() {
    CharArray ca = new CharArray("abc".toCharArray(), 0, 3, false);
    Assert.assertEquals('a', ca.charAt(0));
    Assert.assertEquals('b', ca.charAt(1));
    Assert.assertEquals('c', ca.charAt(2));
  }

  @Test
  public void testSubSequence() {
    CharArray ca = new CharArray("abcde".toCharArray(), 0, 5, false);
    Assert.assertEquals("abc", ca.subSequence(0, 3).toString());
    Assert.assertEquals("de", ca.subSequence(3, 5).toString());
  }

  @Test
  public void testEquals() {
    CharArray ca1 = new CharArray("abc".toCharArray(), 0, 3, false);
    CharArray ca2 = new CharArray("abc".toCharArray(), 0, 3, false);
    CharArray ca3 = new CharArray("xyz".toCharArray(), 0, 3, false);
    Assert.assertEquals(ca1, ca2);
    Assert.assertNotEquals(ca1, ca3);
  }

  @Test
  public void testAppendTo() throws IOException {
    StringBuilder sb = new StringBuilder();
    CharArray ca = new CharArray("abc".toCharArray(), 0, 3, false);
    ca.appendTo(sb);
    Assert.assertEquals("abc", sb.toString());
  }

  @Test
  public void testUpperCase() {
    CharArray ca = new CharArray("abc".toCharArray(), 0, 3, false);
    Assert.assertEquals("ABC", ca.upperCase().toString());
  }

  @Test
  public void testIsEmpty() {
    CharArray ca1 = new CharArray("".toCharArray(), 0, 0, false);
    CharArray ca2 = new CharArray("abc".toCharArray(), 0, 3, false);
    Assert.assertTrue(ca1.isEmpty());
    Assert.assertFalse(ca2.isEmpty());
  }

  @Test
  public void testIndexOf() {
    CharArray ca = new CharArray("abc".toCharArray(), 0, 3, false);
    Assert.assertEquals(0, ca.indexOf('a'));
    Assert.assertEquals(1, ca.indexOf('b'));
    Assert.assertEquals(2, ca.indexOf('c'));
    Assert.assertEquals(-1, ca.indexOf('z'));
  }
}
