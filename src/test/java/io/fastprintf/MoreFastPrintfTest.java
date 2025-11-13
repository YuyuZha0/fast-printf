package io.fastprintf;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;

public class MoreFastPrintfTest {

  // --- Basic Formatting Tests ---

  @Test
  public void testBasicStringAndInteger() {
    FastPrintf f = FastPrintf.compile("User: %s, ID: %d");
    assertEquals("User: test, ID: 123", f.format("test", 123));
  }

  @Test
  public void testAllIntegerTypes() {
    FastPrintf f = FastPrintf.compile("%d, %d, %d, %d, %d");
    byte b = 10;
    short s = 20;
    int i = 30;
    long l = 40L;
    BigInteger bi = new BigInteger("50");
    assertEquals("10, 20, 30, 40, 50", f.format(b, s, i, l, bi));
  }

  @Test
  public void testAllFloatTypes() {
    FastPrintf f = FastPrintf.compile("%.1f, %.1f, %.1f");
    float fl = 1.2f;
    double d = 3.4;
    BigDecimal bd = new BigDecimal("5.67");
    assertEquals("1.2, 3.4, 5.7", f.format(fl, d, bd));
  }

  @Test
  public void testFlagsAndWidth() {
    FastPrintf f = FastPrintf.compile("|%05d|%-5s|%#x|");
    assertEquals("|00123|abc  |0xff|", f.format(123, "abc", 255));
  }

  @Test
  public void testPrecedingWidthAndPrecision() {
    FastPrintf f = FastPrintf.compile("|%*.*f|");
    assertEquals("|   3.14|", f.format(7, 2, 3.14159));
  }

  @Test
  public void testNegativePrecedingWidthForLeftJustify() {
    FastPrintf f = FastPrintf.compile("|%*s|");
    assertEquals("|abc  |", f.format(-5, "abc"));
  }

  @Test
  public void testStringUppercaseSpecifier() {
    FastPrintf f = FastPrintf.compile("Hello, %S!");
    assertEquals("Hello, WORLD!", f.format("world"));
  }

  @Test
  public void testDateTimeSpecifierWithPattern() {
    FastPrintf f = FastPrintf.compile("Date: %t{yyyy-MM-dd}");
    LocalDate date = LocalDate.of(2023, 10, 27);
    assertEquals("Date: 2023-10-27", f.format(date));
  }

  @Test
  public void testDateTimeSpecifierWithDefaultFormatting() {
    FastPrintf f = FastPrintf.compile("%t");
    ZonedDateTime zdt = ZonedDateTime.of(2023, 10, 27, 10, 30, 0, 0, ZoneOffset.UTC);
    // Default for ZonedDateTime is ISO_OFFSET_DATE_TIME
    assertEquals("2023-10-27T10:30:00Z", f.format(zdt));
  }

  @Test
  public void testPointerSpecifier() {
    FastPrintf f = FastPrintf.compile("%p");
    String s = "test object";
    String expected =
        s.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(s));
    assertEquals(expected, f.format(s));
  }

  // --- API Behavior Tests ---

  @Test
  public void testFormatToAppendable() throws IOException {
    FastPrintf f = FastPrintf.compile("Append: %s");
    StringWriter writer = new StringWriter();
    f.format(writer, Args.of("data"));
    assertEquals("Append: data", writer.toString());
  }

  @Test
  public void testFormatToStringBuilder() {
    FastPrintf f = FastPrintf.compile("Append: %s");
    StringBuilder sb = new StringBuilder("Prefix; ");
    f.format(sb, Args.of("data"));
    assertEquals("Prefix; Append: data", sb.toString());
  }

  @Test
  public void testSetStringBuilderInitialCapacity() {
    FastPrintf base = FastPrintf.compile("%d");
    FastPrintf optimized = base.setStringBuilderInitialCapacity(256);
    assertNotSame(base, optimized);
    // Verify it doesn't create a new one if capacity is the same
    assertSame(optimized, optimized.setStringBuilderInitialCapacity(256));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStringBuilderInitialCapacity_throwsOnNegative() {
    FastPrintf.compile("%d").setStringBuilderInitialCapacity(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStringBuilderInitialCapacity_throwsOnZero() {
    FastPrintf.compile("%d").setStringBuilderInitialCapacity(0);
  }

  @Test
  public void testEnableThreadLocalCache() {
    FastPrintf base = FastPrintf.compile("%d");
    FastPrintf cached = base.enableThreadLocalCache();
    assertNotSame(base, cached);
    // Verify it returns itself if already enabled
    assertSame(cached, cached.enableThreadLocalCache());

    // Test that formatting still works
    assertEquals("123", cached.format(123));
  }

  @Test
  public void testImmutabilityOfConfigurationMethods() {
    FastPrintf base = FastPrintf.compile("%d");
    FastPrintf withCapacity = base.setStringBuilderInitialCapacity(50);
    FastPrintf withCache = base.enableThreadLocalCache();

    // Ensure original is not modified
    assertNotSame(base, withCapacity);
    assertNotSame(base, withCache);

    // Ensure methods create new instances from each other
    FastPrintf cachedThenCapacity = withCache.setStringBuilderInitialCapacity(100);
    assertNotSame(withCache, cachedThenCapacity);

    FastPrintf capacityThenCached = withCapacity.enableThreadLocalCache();
    assertNotSame(withCapacity, capacityThenCached);
  }

  // --- Exception Handling Tests ---

  @Test(expected = PrintfSyntaxException.class)
  public void testCompile_throwsOnUnknownSpecifier() {
    FastPrintf.compile("Unknown specifier: %Q");
  }

  @Test(expected = PrintfException.class)
  public void testFormat_throwsOnMissingArgument() {
    FastPrintf f = FastPrintf.compile("%d %d");
    f.format(1); // Missing the second argument
  }

  @Test
  public void testFormat_ignoresExtraArguments() {
    FastPrintf f = FastPrintf.compile("%d");
    // This should not throw an exception
    assertEquals("1", f.format(1, 2, 3));
  }

  @Test
  public void testDefault_formatWithArgs() {
    // This test explicitly calls the `String format(Args args)` default method.
    // This is necessary to ensure code coverage tools see this path as tested.
    FastPrintf f = FastPrintf.compile("Hello %s, value is %d");
    Args args = Args.create().putString("World").putInt(42);

    String result = f.format(args);

    assertEquals("Hello World, value is 42", result);
  }

  @Test
  public void testDefault_formatWithVarargs() {
    // This test explicitly calls the `String format(Object... values)` default method.
    // While other tests may implicitly call this, having a dedicated test makes
    // the intent to cover this specific default method clear.
    FastPrintf f = FastPrintf.compile("Numbers: %.1f and %x");

    String result = f.format(12.345, 255);

    assertEquals("Numbers: 12.3 and ff", result);
  }

  @Test
  public void testDefault_formatWithNoArgs() {
    // Edge case for the default varargs method.
    FastPrintf f = FastPrintf.compile("Static text");
    String result = f.format(); // Calls format() with a zero-length array
    assertEquals("Static text", result);
  }
}
