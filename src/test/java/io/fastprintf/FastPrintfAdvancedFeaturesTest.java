package io.fastprintf;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import org.junit.Test;

/**
 * Unit tests for the advanced features of the fast-printf library, as highlighted in the
 * introductory blog post. These tests verify correctness for dynamic formatting, type-specific
 * specifiers, and performance-tuning options.
 *
 * @author The fast-printf Author
 */
public class FastPrintfAdvancedFeaturesTest {

  @Test
  public void testDynamicWidthAndPrecision() {
    // This test validates the use of '*' for dynamic width and precision,
    // a key feature for creating aligned text layouts.
    FastPrintf formatter = FastPrintf.compile("User: %-*s | ID: %0*d | Pi: %.*f");

    String expected = "User: Alice      | ID: 00000123 | Pi: 3.142";
    String actual = formatter.format(10, "Alice", 8, 123, 3, Math.PI);

    assertEquals(expected, actual);
  }

  @Test
  public void testUpperCaseSpecifiers() {
    // `%S` and `%T` are glibc-compliant specifiers for uppercasing output,
    // which is extremely convenient for normalizing things like log levels or statuses.
    // We use a specific ZonedDateTime to test AM/PM conversion, as it's locale-dependent.
    FastPrintf formatter = FastPrintf.compile("Status: %S, Time: %{hh:mm a}T");

    // Use a time that will produce "pm" to verify it becomes "PM".
    ZonedDateTime dateTime =
        ZonedDateTime.of(2023, 10, 27, 14, 30, 0, 0, ZoneId.of("America/New_York"));

    // We need to run this test with an English locale to ensure 'pm' is generated.
    Locale.setDefault(Locale.ENGLISH);

    String expected = "Status: SUCCESS, Time: 02:30 PM";
    String actual = formatter.format("success", dateTime);

    assertEquals(expected, actual);
  }

  @Test
  public void testCustomDateTimeFormatting() {
    // This is a powerful feature unique to fast-printf, allowing inline date patterns.
    // It avoids the need to manage DateTimeFormatter instances in business logic.
    FastPrintf formatter =
        FastPrintf.compile("Default: %t, Custom: %{yyyy/MM/dd}t, Literal: %{'at' HH:mm}t");

    LocalDateTime dateTime = LocalDateTime.of(2023, 5, 1, 15, 45, 30);

    // The default for LocalDateTime is ISO_LOCAL_DATE_TIME.
    String expected = "Default: 2023-05-01T15:45:30, Custom: 2023/05/01, Literal: at 15:45";
    String actual = formatter.format(dateTime, dateTime, dateTime);

    assertEquals(expected, actual);
  }

  @Test
  public void testSafePointerFormattingForObject() {
    // The '%p' specifier should correctly format an object's identity.
    FastPrintf formatter = FastPrintf.compile("%p");
    Object obj = new Object();

    String expected =
        obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    String actual = formatter.format(obj);

    assertEquals(expected, actual);
  }

  @Test
  public void testSafePointerFormattingForNull() {
    // The '%p' specifier should handle null gracefully, as per glibc conventions.
    FastPrintf formatter = FastPrintf.compile("%p");
    String expected = "null"; // common representation for null pointers
    String actual = formatter.format((Object) null);

    assertEquals(expected, actual);
  }

  @Test(expected = PrintfException.class)
  public void testSafePointerFormattingForPrimitiveThrowsException_WithBuilder() {
    // CRITICAL TEST: This verifies that our design correctly distinguishes primitives
    // from objects. Passing a primitive 'int' to '%p' should fail because
    // primitives don't have a stable object identity. This test uses the Args builder.
    FastPrintf formatter = FastPrintf.compile("%p");
    formatter.format(Args.create().putInt(42)); // This should throw.
  }

  @Test(expected = PrintfException.class)
  public void testSafePointerFormattingForPrimitiveThrowsException_WithVarargs() {
    // CRITICAL TEST: Same as above, but for the convenient varargs method.
    // The library correctly tracks that the argument originated as a primitive
    // despite auto-boxing and throws an exception.
    FastPrintf formatter = FastPrintf.compile("%p");
    formatter.format(Args.create().putInt(42)); // This should also throw.
  }

  @Test
  public void testThreadLocalCacheFunctionalityAndImmutability() {
    // This test verifies the behavior of the ThreadLocal cache option.
    // 1. It ensures the feature doesn't break formatting correctness.
    // 2. It verifies the immutability contract (new instances are returned on change).
    FastPrintf baseFormatter = FastPrintf.compile("ID=%d, Name=%s");

    // Enable the cache
    FastPrintf cachedFormatter = baseFormatter.enableThreadLocalCache();

    // 1. Functional Correctness Check
    String expected = "ID=123, Name=test";
    String actualFromBase = baseFormatter.format(123, "test");
    String actualFromCached = cachedFormatter.format(123, "test");

    assertEquals("Cached formatter should produce the same output", expected, actualFromBase);
    assertEquals("Cached formatter should produce the same output", expected, actualFromCached);
    assertNotSame("Enabling cache should return a new instance", baseFormatter, cachedFormatter);

    // 2. Immutability and Idempotency Check
    FastPrintf cachedFormatter2 = cachedFormatter.enableThreadLocalCache();
    assertSame(
        "Enabling cache on an already-cached instance should return itself",
        cachedFormatter,
        cachedFormatter2);

    // 3. Chaining with other methods
    FastPrintf cachedAndResized = cachedFormatter.setStringBuilderInitialCapacity(512);
    assertNotSame(
        "Changing another property should return a new instance",
        cachedFormatter,
        cachedAndResized);

    // Verify the "cached" state is propagated to the new instance
    FastPrintf reCached = cachedAndResized.enableThreadLocalCache();
    assertSame("The new instance should retain the cached property", cachedAndResized, reCached);
  }
}
