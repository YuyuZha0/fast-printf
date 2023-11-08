package io.fastprintf.util;

import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

  @Test
  public void testUpperCase() {
    char[] chars = {'a', 'b', 'c'};
    Utils.toUpperCase(chars);
    assertArrayEquals(new char[] {'A', 'B', 'C'}, chars);

    chars = new char[] {'A', 'B', 'C'};
    assertFalse(Utils.toUpperCase(chars));

    assertEquals("ABC", Utils.toUpperCase("abc"));
    assertEquals("ABC", Utils.toUpperCase("ABC"));
    assertEquals("123", Utils.toUpperCase("123"));
    assertEquals("", Utils.toUpperCase(""));
  }

  @Test
  public void testIsLowerCase() {
    assertTrue(Utils.isLowerCase('a'));
    assertFalse(Utils.isLowerCase('A'));
    assertFalse(Utils.isLowerCase('1'));
    assertFalse(Utils.isLowerCase('@'));
  }

  @Test
  public void testToUpperCaseChar() {
    assertEquals('A', Utils.toUpperCase('a'));
    assertEquals('A', Utils.toUpperCase('A'));
  }

  @Test
  public void testIsDigit() {
    for (char c = '0'; c <= '9'; c++) {
      assertTrue(Utils.isDigit(c));
    }
    assertFalse(Utils.isDigit('a'));
    assertFalse(Utils.isDigit('A'));
  }

  @Test
  public void testIsNotDigit() {
    for (char c = '0'; c <= '9'; c++) {
      assertFalse(Utils.isNotDigit(c));
    }
    assertTrue(Utils.isNotDigit('a'));
    assertTrue(Utils.isNotDigit('A'));
  }

  @Test
  public void testJoin() {
    assertEquals("1,2,3,4", Utils.join(",", Arrays.asList(1, 2, 3, 4)));
    assertEquals("", Utils.join(",", new Object[0]));
    assertEquals("a,b,c", Utils.join(",", new Object[] {"a", "b", "c"}));
  }

  @Test
  public void testLongToInstant() {
    assertEquals(Instant.ofEpochSecond(5000), Utils.longToInstant(5000));
    assertEquals(Instant.ofEpochMilli(5000000000L), Utils.longToInstant(5000000000L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLongToInstantNegative() {
    Utils.longToInstant(-1000);
  }

  @Test
  public void testLenientFormat() {
    assertEquals("1, 2, 3", Utils.lenientFormat("%s, %s, %s", 1, 2, 3));
    assertEquals("test [extra, args]", Utils.lenientFormat("test", "extra", "args"));
    assertEquals("null", Utils.lenientFormat(null, "test"));
  }
}
