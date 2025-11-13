package io.fastprintf;

import static org.junit.Assert.*;

import org.junit.Test;

public class PrintfSyntaxExceptionTest {

  private static final String nl = System.lineSeparator();

  @Test
  public void testGettersAndConstructor() {
    String desc = "Unknown format conversion specifier 'Z'";
    String pattern = "Hello %Z World";
    int index = 7;
    PrintfSyntaxException ex = new PrintfSyntaxException(desc, pattern, index);

    assertEquals(desc, ex.getDescription());
    assertEquals(pattern, ex.getPattern());
    assertEquals(index, ex.getIndex());
    assertNull(ex.getCause());
  }

  @Test
  public void testGettersAndConstructorWithCause() {
    String desc = "Invalid date/time pattern";
    String pattern = "%t{YYYY-MM-DD}";
    int index = 3;
    Throwable cause = new IllegalArgumentException("Invalid pattern letter: D");
    PrintfSyntaxException ex = new PrintfSyntaxException(desc, pattern, index, cause);

    assertEquals(desc, ex.getDescription());
    assertEquals(pattern, ex.getPattern());
    assertEquals(index, ex.getIndex());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testGetMessage_withValidIndex() {
    String desc = "Unknown specifier";
    String pattern = "An error: %Q";
    int index = 11;
    PrintfSyntaxException ex = new PrintfSyntaxException(desc, pattern, index);

    String expected = "Unknown specifier near index 11" + nl + "An error: %Q" + nl + "           ^";
    assertEquals(expected, ex.getMessage());
  }

  @Test
  public void testGetMessage_withNegativeIndex() {
    String desc = "Terminates prematurely";
    String pattern = "%";
    int index = -1; // Index is not known
    PrintfSyntaxException ex = new PrintfSyntaxException(desc, pattern, index);

    // The "near index" part and the pointer line should be absent
    String expected = "Terminates prematurely" + nl + "%";
    assertEquals(expected, ex.getMessage());
  }

  @Test
  public void testGetMessage_withIndexZero() {
    String desc = "Invalid flag";
    String pattern = "%?d";
    int index = 1;
    PrintfSyntaxException ex = new PrintfSyntaxException(desc, pattern, index);

    String expected = "Invalid flag near index 1" + nl + "%?d" + nl + " ^";
    assertEquals(expected, ex.getMessage());
  }

  @Test
  public void testGetMessage_withNullPattern() {
    String desc = "Null pattern provided";
    PrintfSyntaxException ex = new PrintfSyntaxException(desc, null, 5);

    // Should not throw NPE and should format gracefully
    String expected = "Null pattern provided near index 5" + nl + "null";
    assertEquals(expected, ex.getMessage());
  }
}
