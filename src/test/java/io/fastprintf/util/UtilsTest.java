package io.fastprintf.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

  @Test
  public void testToUpperCaseString() {
    assertEquals("ABC", Utils.toUpperCase("abc"));
    assertEquals("ABC", Utils.toUpperCase("ABC"));
    assertEquals("ABC", Utils.toUpperCase("AbC"));
    assertEquals("ABC", Utils.toUpperCase("aBc"));
    assertEquals("ABC", Utils.toUpperCase("Abc"));
    assertEquals("ABC", Utils.toUpperCase("abC"));
    assertEquals("ABC", Utils.toUpperCase("aBC"));

    assertEquals("", Utils.toUpperCase(""));
    assertEquals("A", Utils.toUpperCase("a"));
    assertEquals("测试@1", Utils.toUpperCase("测试@1"));
  }

  @Test
  public void testToUpperCaseChar() {
    assertEquals('A', Utils.toUpperCase('a'));
    assertEquals('A', Utils.toUpperCase('A'));
    assertEquals('@', Utils.toUpperCase('@'));
    assertEquals('1', Utils.toUpperCase('1'));
    assertEquals('测', Utils.toUpperCase('测'));
  }

  @Test
  public void testToUpperCaseCharArray() {

    char[] chars = new char[] {'a', 'b', 'c'};
    Utils.toUpperCase(chars);
    assertEquals('A', chars[0]);
    assertEquals('B', chars[1]);
    assertEquals('C', chars[2]);
  }

  @Test
  public void testIsDigit() {

    assertTrue(Utils.isDigit('0'));
    assertTrue(Utils.isDigit('1'));
    assertTrue(Utils.isDigit('2'));
    assertTrue(Utils.isDigit('3'));
    assertTrue(Utils.isDigit('4'));
    assertTrue(Utils.isDigit('5'));
    assertTrue(Utils.isDigit('6'));
    assertTrue(Utils.isDigit('7'));
    assertTrue(Utils.isDigit('8'));
    assertTrue(Utils.isDigit('9'));

    assertFalse(Utils.isDigit('a'));
    assertFalse(Utils.isDigit('b'));
    assertFalse(Utils.isDigit('一'));
    assertFalse(Utils.isDigit(' '));
  }

  @Test
  public void testJoin() {
    assertEquals("1,2,3", Utils.join(",", new String[] {"1", "2", "3"}));
    assertEquals("1,2,3", Utils.join(",", new Integer[] {1, 2, 3}));
    assertEquals("1,2,3", Utils.join(",", new int[] {1, 2, 3}));
    assertEquals("1,2,3", Utils.join(",", new long[] {1, 2, 3}));
    assertEquals("1,2,3", Utils.join(",", Arrays.asList(1, 2, 3)));
    assertEquals("1,2,3", Utils.join(",", Arrays.asList("1", "2", "3")));
    assertEquals("1,2,3", Utils.join(",", Arrays.asList(1, 2, 3).iterator()));
    assertEquals("1,2,3", Utils.join(",", Arrays.asList("1", "2", "3").iterator()));
    assertEquals("", Utils.join(",", null));
    assertEquals("", Utils.join(",", new String[] {}));
    assertEquals("1,2,null", Utils.join(",", new Integer[] {1, 2, null}));
  }
}
