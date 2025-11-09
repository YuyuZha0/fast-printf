package io.fastprintf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import org.junit.Test;

public class FastPrintfImplTest {

  @Test
  public void format_withVariousSpecifiers_shouldProduceCorrectString() {
    FastPrintf formatter = FastPrintf.compile("Name: %s, ID: %04d, Temp: %.2f%%, Hex: %#X");
    String result = formatter.format("test", 7, 98.615, 255);
    assertEquals("Name: test, ID: 0007, Temp: 98.62%, Hex: 0XFF", result);
  }

  @Test
  public void format_withVarargs_shouldProduceCorrectString() {
    FastPrintf formatter = FastPrintf.compile("Hello %s, number %d");
    String result = formatter.format("World", 123);
    assertEquals("Hello World, number 123", result);
  }

  @Test
  public void format_withArgsObject_shouldProduceCorrectString() {
    FastPrintf formatter = FastPrintf.compile("Hello %s, number %d");
    Args args = Args.create().putString("World").putInt(123);
    String result = formatter.format(args);
    assertEquals("Hello World, number 123", result);
  }

  @Test
  public void format_toStringBuilder_shouldAppendCorrectly() {
    FastPrintf formatter = FastPrintf.compile("-> %s");
    StringBuilder sb = new StringBuilder("Existing ");
    formatter.format(sb, Args.of("data"));
    assertEquals("Existing -> data", sb.toString());
  }

  @Test
  public void format_toGenericAppendable_shouldAppendCorrectly() {
    FastPrintf formatter = FastPrintf.compile("-> %s");
    StringWriter writer = new StringWriter();
    writer.write("Existing ");
    formatter.format(writer, Args.of("data"));
    assertEquals("Existing -> data", writer.toString());
  }

  @Test(expected = PrintfException.class)
  public void format_withInsufficientArguments_shouldThrowException() {
    FastPrintf formatter = FastPrintf.compile("A: %d, B: %s");
    formatter.format(123); // This should throw
  }

  @Test
  public void enableThreadLocalCache_shouldReturnNewInstanceFirstTime() {
    FastPrintf formatter = FastPrintf.compile("test");
    FastPrintf cachedFormatter = formatter.enableThreadLocalCache();
    assertNotSame(formatter, cachedFormatter);
    assertEquals("test", cachedFormatter.format());
  }

  @Test
  public void enableThreadLocalCache_shouldReturnSameInstanceOnSecondCall() {
    FastPrintf formatter = FastPrintf.compile("test");
    FastPrintf cachedFormatter1 = formatter.enableThreadLocalCache();
    FastPrintf cachedFormatter2 = cachedFormatter1.enableThreadLocalCache();
    assertSame(cachedFormatter1, cachedFormatter2);
  }

  @Test
  public void setStringBuilderInitialCapacity_shouldReturnNewInstanceForNewCapacity() {
    FastPrintf formatter = FastPrintf.compile("test %d");
    FastPrintf newFormatter = formatter.setStringBuilderInitialCapacity(256);
    assertNotSame(formatter, newFormatter);
    assertEquals("test 123", newFormatter.format(123));
  }

  @Test
  public void setStringBuilderInitialCapacity_shouldReturnSameInstanceForSameCapacity() {
    FastPrintf formatter = FastPrintf.compile("test");
    // Get the internal default capacity by creating a new instance with a known value
    FastPrintf formatterWithKnownCapacity = formatter.setStringBuilderInitialCapacity(100);
    // Setting it again to the same value should be a no-op
    FastPrintf sameFormatter = formatterWithKnownCapacity.setStringBuilderInitialCapacity(100);
    assertSame(formatterWithKnownCapacity, sameFormatter);
  }

  @Test
  public void setStringBuilderInitialCapacity_withInvalidCapacity_shouldThrowException() {
    FastPrintf formatter = FastPrintf.compile("test");
    try {
      formatter.setStringBuilderInitialCapacity(0);
      fail("Expected IllegalArgumentException was not thrown for capacity 0");
    } catch (IllegalArgumentException expected) {
      // Test passed
    }
    try {
      formatter.setStringBuilderInitialCapacity(-1);
      fail("Expected IllegalArgumentException was not thrown for capacity -1");
    } catch (IllegalArgumentException expected) {
      // Test passed
    }
  }

  @Test
  public void format_withLiteralPercent_shouldWorkCorrectly() {
    FastPrintf formatter = FastPrintf.compile("Rate is 100%%");
    assertEquals("Rate is 100%", formatter.format());
  }
}
