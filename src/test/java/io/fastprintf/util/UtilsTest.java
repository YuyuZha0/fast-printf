package io.fastprintf.util;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class UtilsTest {

  // --- toUpperCase Tests ---

  @Test
  public void testToUpperCase_CharArray() {
    char[] mixed = {'a', 'B', 'c', '1', ' '};
    assertTrue("Should return true as modification occurred", Utils.toUpperCase(mixed));
    assertArrayEquals(new char[] {'A', 'B', 'C', '1', ' '}, mixed);

    char[] alreadyUpper = {'A', 'B', 'C'};
    assertFalse("Should return false as no modification occurred", Utils.toUpperCase(alreadyUpper));
    assertArrayEquals(new char[] {'A', 'B', 'C'}, alreadyUpper);

    char[] empty = {};
    assertFalse("Should return false for empty array", Utils.toUpperCase(empty));
  }

  @Test
  public void testToUpperCase_String() {
    assertEquals("MIXED CASE 123", Utils.toUpperCase("Mixed Case 123"));
    assertEquals("ALREADY UPPER", Utils.toUpperCase("ALREADY UPPER"));
    assertEquals("123456$#", Utils.toUpperCase("123456$#"));
    assertEquals("", Utils.toUpperCase(""));

    // Test that it returns the same object if no changes are made
    String original = "NO CHANGE";
    assertSame(original, Utils.toUpperCase(original));
  }

  @Test
  public void testToUpperCase_char() {
    assertEquals('A', Utils.toUpperCase('a'));
    assertEquals('Z', Utils.toUpperCase('z'));
    assertEquals('A', Utils.toUpperCase('A')); // No change
    assertEquals('1', Utils.toUpperCase('1')); // No change
    assertEquals(' ', Utils.toUpperCase(' ')); // No change
  }

  // --- Character Type Tests ---

  @Test
  public void testIsLowerCase() {
    assertTrue(Utils.isLowerCase('a'));
    assertTrue(Utils.isLowerCase('z'));
    assertFalse(Utils.isLowerCase('A'));
    assertFalse(Utils.isLowerCase('Z'));
    assertFalse(Utils.isLowerCase('5'));
    assertFalse(Utils.isLowerCase('$'));
  }

  @Test
  public void testIsDigit() {
    assertTrue(Utils.isDigit('0'));
    assertTrue(Utils.isDigit('9'));
    assertFalse(Utils.isDigit('a'));
    assertFalse(Utils.isDigit(' '));
  }

  @Test
  public void testIsNotDigit() {
    assertFalse(Utils.isNotDigit('0'));
    assertFalse(Utils.isNotDigit('9'));
    assertTrue(Utils.isNotDigit('a'));
    assertTrue(Utils.isNotDigit(' '));
  }

  // --- Join Tests ---

  @Test
  public void testJoin_String() {
    assertEquals("1,2,3", Utils.join(",", new int[] {1, 2, 3}));
    assertEquals("a|b|c", Utils.join("|", Arrays.asList("a", "b", "c")));
    assertEquals("", Utils.join(",", new Object[0]));
  }

  @Test
  public void testJoin_StringBuilder() {
    StringBuilder sb = new StringBuilder("Prefix:");
    Utils.join(sb, " ", new String[] {"one", "two"});
    assertEquals("Prefix:one two", sb.toString());

    // Test with different types
    sb = new StringBuilder();
    Utils.join(sb, "-", Arrays.asList(1, "two", 3.0));
    assertEquals("1-two-3.0", sb.toString());

    // Test with iterator
    sb = new StringBuilder();
    List<String> list = Arrays.asList("x", "y", "z");
    Utils.join(sb, ",", list.iterator());
    assertEquals("x,y,z", sb.toString());

    // Test with single object
    sb = new StringBuilder();
    Utils.join(sb, ",", 42);
    assertEquals("42", sb.toString());

    // Test null args
    sb = new StringBuilder("test");
    Utils.join(sb, ",", null);
    assertEquals("test", sb.toString());

    // Test empty collections
    sb = new StringBuilder("test");
    Utils.join(sb, ",", new ArrayList<>());
    assertEquals("test", sb.toString());
  }

  // --- longToInstant Tests ---

  @Test
  public void testLongToInstant() {
    // Test value within epoch-second range
    assertEquals(Instant.ofEpochSecond(100), Utils.longToInstant(100L));

    // Test value at the boundary
    long maxUnsignedInt = 0xFFFFFFFFL;
    assertEquals(Instant.ofEpochSecond(maxUnsignedInt), Utils.longToInstant(maxUnsignedInt));

    // Test value in epoch-milli range
    long milliValue = maxUnsignedInt + 1;
    assertEquals(Instant.ofEpochMilli(milliValue), Utils.longToInstant(milliValue));

    // Test zero
    assertEquals(Instant.ofEpochSecond(0), Utils.longToInstant(0L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLongToInstant_NegativeValue() {
    Utils.longToInstant(-1L);
  }

  // --- Lenient String Helper Tests ---

  @Test
  public void testLenientToString_NormalObject() {
    // Using reflection to access private method for direct testing
    try {
      java.lang.reflect.Method method =
          Utils.class.getDeclaredMethod("lenientToString", Object.class);
      method.setAccessible(true);
      assertEquals("hello", method.invoke(null, "hello"));
      assertEquals("123", method.invoke(null, 123));
      assertEquals("null", method.invoke(null, (Object) null));
    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  public void testLenientToString_ObjectWithFailingToString() {
    Object badObject =
        new Object() {
          @Override
          public String toString() {
            throw new RuntimeException("toString failed!");
          }
        };

    try {
      java.lang.reflect.Method method =
          Utils.class.getDeclaredMethod("lenientToString", Object.class);
      method.setAccessible(true);
      String result = (String) method.invoke(null, badObject);
      assertTrue(result.contains(badObject.getClass().getName()));
      assertTrue(result.contains("threw java.lang.RuntimeException"));
    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  // --- lenientFormat Tests ---

  @Test
  public void testLenientFormat_Basic() {
    assertEquals("First: 1, Second: two", Utils.lenientFormat("First: %s, Second: %s", 1, "two"));
    assertEquals("No placeholders", Utils.lenientFormat("No placeholders"));
  }

  @Test
  public void testLenientFormat_ExtraArgs() {
    assertEquals(
        "One placeholder: foo [bar, 123]",
        Utils.lenientFormat("One placeholder: %s", "foo", "bar", 123));
  }

  @Test
  public void testLenientFormat_NotEnoughArgs() {
    assertEquals(
        "Missing arg: %s", Utils.lenientFormat("Missing arg: %s"));
    assertEquals(
        "Two missing: %s, %s", Utils.lenientFormat("Two missing: %s, %s"));
  }

  @Test
  public void testLenientFormat_Nulls() {
    assertEquals("Template is null", "null", Utils.lenientFormat(null, "arg"));
    assertEquals(
        "Args array is null: (Object[])null",
        Utils.lenientFormat("Args array is null: %s", (Object[]) null));
    assertEquals(
        "Arg element is null: null", Utils.lenientFormat("Arg element is null: %s", (Object) null));
  }

  @Test
  public void testLenientFormat_NoPlaceholdersWithArgs() {
    assertEquals(
        "No placeholders [arg1, arg2]", Utils.lenientFormat("No placeholders", "arg1", "arg2"));
  }
}
