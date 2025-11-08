package io.fastprintf.appender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class FixedStringAppenderTest {

  @Test
  public void testConstructorAndGetValue() {
    String expectedValue = "Hello, World!";
    FixedStringAppender appender = new FixedStringAppender(expectedValue);
    assertNotNull("Appender should not be null", appender);
    assertEquals(
        "getValue() should return the original string", expectedValue, appender.getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorWithNull() {
    // The constructor should throw a NullPointerException for a null input value,
    // as enforced by Preconditions.checkNotNull().
    new FixedStringAppender(null);
  }

  @Test
  public void testAppend() {
    String testValue = "fixed literal";
    FixedStringAppender appender = new FixedStringAppender(testValue);
    List<Seq> seqList = new ArrayList<>();
    Iterator<FormatTraits> nullIterator = null; // The iterator is not used by this appender.

    appender.append(seqList::add, nullIterator);

    assertEquals("List should contain exactly one element after append", 1, seqList.size());
    assertEquals(
        "The appended sequence should match the appender's value",
        testValue,
        seqList.get(0).toString());
  }

  @Test
  public void testToString_SimpleString() {
    FixedStringAppender appender = new FixedStringAppender("simple text");
    // Expected output is the string literal representation: "simple text"
    assertEquals("\"simple text\"", appender.toString());
  }

  @Test
  public void testToString_EmptyString() {
    FixedStringAppender appender = new FixedStringAppender("");
    // Expected output for an empty string is ""
    assertEquals("\"\"", appender.toString());
  }

  @Test
  public void testToString_WithWhitespaceEscapes() {
    String valueWithWhitespace = "Line 1\nLine 2\r\tIndented";
    FixedStringAppender appender = new FixedStringAppender(valueWithWhitespace);
    // Expected output with escaped whitespace characters.
    String expected = "\"Line 1\\nLine 2\\r\\tIndented\"";
    assertEquals(
        "toString should correctly escape whitespace characters", expected, appender.toString());
  }

  @Test
  public void testToString_WithBackslashAndQuote() {
    String valueWithEscapes = "Path: C:\\Program Files\\\"App\"";
    FixedStringAppender appender = new FixedStringAppender(valueWithEscapes);
    // Expected output with escaped backslashes and double quotes.
    String expected = "\"Path: C:\\\\Program Files\\\\\\\"App\\\"\"";
    assertEquals(
        "toString should correctly escape backslashes and double quotes",
        expected,
        appender.toString());
  }

  @Test
  public void testToString_WithAllSpecialCharacters() {
    String allSpecial = "\n\r\t\b\f\\\"";
    FixedStringAppender appender = new FixedStringAppender(allSpecial);
    String expected = "\"\\n\\r\\t\\b\\f\\\\\\\"\"";
    assertEquals(
        "toString should handle all special characters together", expected, appender.toString());
  }
}
