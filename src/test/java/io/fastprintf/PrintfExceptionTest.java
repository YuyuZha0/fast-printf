package io.fastprintf;

import static org.junit.Assert.*;

import org.junit.Test;

public class PrintfExceptionTest {

  @Test
  public void testConstructorWithMessageAndArgs() {
    PrintfException ex = new PrintfException("Error processing value %s for user %s", 123, "test");
    assertEquals("Error processing value 123 for user test", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructorWithMessageAndCause() {
    Throwable cause = new IllegalArgumentException("underlying cause");
    PrintfException ex = new PrintfException("A formatting error occurred", cause);
    assertEquals("A formatting error occurred", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testConstructorWithCauseOnly() {
    Throwable cause = new NullPointerException();
    PrintfException ex = new PrintfException(cause);
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testDefaultConstructor() {
    PrintfException ex = new PrintfException();
    assertNull(ex.getMessage());
    assertNull(ex.getCause());
  }
}
