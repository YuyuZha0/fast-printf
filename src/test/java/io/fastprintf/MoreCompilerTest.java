package io.fastprintf;

import static org.junit.Assert.*;

import io.fastprintf.appender.Appender;
import io.fastprintf.appender.DefaultAppender;
import io.fastprintf.appender.FixedStringAppender;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

/**
 * A suite of "battle tests" for the new Compiler implementation. These tests focus on complex edge
 * cases, invalid syntax, and interactions between the new date/time pattern syntax and other format
 * specifier components.
 */
public class MoreCompilerTest {

  // region Helper Assertion Methods
  private List<Appender> compile(String format) {
    Compiler compiler = new Compiler(format);
    compiler.compile();
    return compiler.getAppenders();
  }

  private void assertFixed(Appender appender, String value) {
    assertTrue(
        "Expected FixedStringAppender but was " + appender.getClass().getSimpleName(),
        appender instanceof FixedStringAppender);
    assertEquals(value, ((FixedStringAppender) appender).getValue());
  }

  private DefaultAppender assertPattern(Appender appender, Specifier specifier) {
    assertTrue(
        "Expected DefaultAppender but was " + appender.getClass().getSimpleName(),
        appender instanceof DefaultAppender);
    DefaultAppender da = (DefaultAppender) appender;
    assertEquals(specifier, da.getSpecifier());
    return da;
  }

  private void assertContext(DefaultAppender da, EnumSet<Flag> flags, int width, int precision) {
    FormatContext context = da.getContext();
    assertEquals(flags, context.getFlags());
    assertEquals(width, context.getWidth());
    assertEquals(precision, context.getPrecision());
  }

  // endregion

  // region Core Functionality Tests for the New Grammar

  @Test
  public void testBasicDateTimePattern() {
    List<Appender> appenders = compile("Date: %t{yyyy-MM-dd}");
    assertEquals(2, appenders.size());
    assertFixed(appenders.get(0), "Date: ");
    DefaultAppender da = assertPattern(appenders.get(1), Specifier.DATE_AND_TIME);
    assertContext(da, EnumSet.noneOf(Flag.class), -1, -1);
    DateTimeFormatter dtf = da.getContext().getDateTimeFormatter();
    assertNotNull("DateTimeFormatter should not be null", dtf);
    assertEquals(LocalDate.of(2024, 6, 15), LocalDate.parse("2024-06-15", dtf));
  }

  @Test
  public void testUppercaseDateTimePattern() {
    List<Appender> appenders = compile("Date: %T{HH:mm:ss}");
    assertEquals(2, appenders.size());
    assertFixed(appenders.get(0), "Date: ");
    DefaultAppender da = assertPattern(appenders.get(1), Specifier.DATE_AND_TIME_UPPERCASE);
    assertNotNull("DateTimeFormatter should not be null", da.getContext().getDateTimeFormatter());
  }

  @Test
  public void testDateTimePatternWithAllModifiers() {
    List<Appender> appenders = compile("Log: %-25.20t{'['HH:mm:ss.SSS']'}");
    assertEquals(2, appenders.size());
    assertFixed(appenders.get(0), "Log: ");
    DefaultAppender da = assertPattern(appenders.get(1), Specifier.DATE_AND_TIME);
    assertContext(da, EnumSet.of(Flag.LEFT_JUSTIFY), 25, 20);
    assertNotNull(da.getContext().getDateTimeFormatter());
  }

  @Test
  public void testDateTimePatternWithoutPatternIsAllowed() {
    List<Appender> appenders = compile("%t");
    assertEquals(1, appenders.size());
    DefaultAppender da = assertPattern(appenders.get(0), Specifier.DATE_AND_TIME);
    assertContext(da, EnumSet.noneOf(Flag.class), -1, -1);
    assertNull(
        "DateTimeFormatter should be null for default formatting",
        da.getContext().getDateTimeFormatter());
  }

  // endregion

  // region Syntax Error and Invalid Combination Tests

  @Test
  public void testBraceBlockAfterNonDateTimeSpecifierIsLiteral() {
    // THIS IS A CRITICAL TEST FOR THE NEW LOGIC.
    // The pattern {...} should only be consumed for %t or %T.
    // For any other specifier, it must be treated as a literal string that follows.
    List<Appender> appenders = compile("%s{this-is-literal} and %d{also-literal}");
    assertEquals(4, appenders.size());

    assertPattern(appenders.get(0), Specifier.STRING);
    assertFixed(appenders.get(1), "{this-is-literal} and ");
    assertPattern(appenders.get(2), Specifier.SIGNED_DECIMAL_INTEGER);
    assertFixed(appenders.get(3), "{also-literal}");
  }

  @Test(expected = PrintfSyntaxException.class)
  public void testUnclosedBraceAfterSpecifier() {
    compile("%t{yyyy-MM-dd");
  }

  @Test(expected = PrintfSyntaxException.class)
  public void testEmptyPatternShouldFail() {
    compile("%t{}");
  }

  @Test(expected = PrintfSyntaxException.class)
  public void testInvalidDateTimePatternSyntax() {
    // 'R' is not a valid pattern char in DateTimeFormatter, this should be caught by
    // DateTimeFormatter.ofPattern
    compile("%t{YYYY-MM-dd-R}");
  }

  @Test
  public void testLiteralCharacterAfterPattern() {
    List<Appender> appenders = compile("%t{HH:mm}s");
    assertEquals(2, appenders.size());
    DefaultAppender da = assertPattern(appenders.get(0), Specifier.DATE_AND_TIME);
    assertNotNull(da.getContext().getDateTimeFormatter());
    assertFixed(appenders.get(1), "s");
  }

  // endregion

  // region Complex Interaction and Edge Case Tests

  @Test
  public void testPatternWithEscapedQuotesAndBraces() {
    // A complex pattern that includes single quotes for escaping and literal braces.
    // This tests the robustness of the pattern scanner.
    String pattern = "yyyy-MM-dd'T''{'HH:mm:ss'}'";
    List<Appender> appenders = compile("%t{" + pattern + "}");
    DefaultAppender da = assertPattern(appenders.get(0), Specifier.DATE_AND_TIME);
    DateTimeFormatter dtf = da.getContext().getDateTimeFormatter();
    assertNotNull(dtf);

    // Verify the formatter works as expected
    String formatted = dtf.format(java.time.LocalDate.of(2024, 1, 1).atStartOfDay());
    assertEquals("2024-01-01T'{00:00:00}", formatted);
  }

  @Test
  public void testMultipleSpecifiersMixed() {
    String format = "Event at %t{HH:mm} on %t{yyyy-MM-dd} for user %s (ID: %04d)%%";
    List<Appender> appenders = compile(format);

    assertEquals(10, appenders.size());
    assertFixed(appenders.get(0), "Event at ");

    DefaultAppender timeAppender = assertPattern(appenders.get(1), Specifier.DATE_AND_TIME);
    assertNotNull(timeAppender.getContext().getDateTimeFormatter());

    assertFixed(appenders.get(2), " on ");

    DefaultAppender dateAppender = assertPattern(appenders.get(3), Specifier.DATE_AND_TIME);
    assertNotNull(dateAppender.getContext().getDateTimeFormatter());

    assertFixed(appenders.get(4), " for user ");
    assertPattern(appenders.get(5), Specifier.STRING);

    assertFixed(appenders.get(6), " (ID: ");

    DefaultAppender idAppender = assertPattern(appenders.get(7), Specifier.SIGNED_DECIMAL_INTEGER);
    assertContext(idAppender, EnumSet.of(Flag.ZERO_PAD), 4, -1);

    assertFixed(appenders.get(8), ")");
    assertFixed(appenders.get(9), "%");
  }

  @Test
  public void testTerminatingWithDateTimePattern() {
    // Ensures the parser doesn't read past the end of the string.
    List<Appender> appenders = compile("%t{HH:mm}");
    assertEquals(1, appenders.size());
    assertPattern(appenders.get(0), Specifier.DATE_AND_TIME);
  }

  @Test
  public void testTerminatingWithUnclosedDateTimePattern() {
    try {
      compile("%t{HH:mm");
      fail("Should throw PrintfSyntaxException for unclosed brace at end of string");
    } catch (PrintfSyntaxException e) {
      assertEquals("Unclosed date/time pattern starting at index 2", e.getDescription());
    }
  }

  @Test
  public void testPatternContainingPercentSymbol() {
    // While not a standard DateTimeFormatter pattern, let's see how the parser handles it.
    // It should be treated as a literal inside quotes.
    String pattern = "HH:mm '%% progress'";
    List<Appender> appenders = compile("%t{" + pattern + "}");
    DefaultAppender da = assertPattern(appenders.get(0), Specifier.DATE_AND_TIME);
    DateTimeFormatter dtf = da.getContext().getDateTimeFormatter();
    assertNotNull(dtf);

    String formatted = dtf.format(java.time.LocalTime.of(14, 30));
    assertEquals("14:30 %% progress", formatted);
  }

  @Test
  public void testDateTimePatternWithDynamicWidthAndPrecision() {
    // While width/precision don't affect DateTimeFormatter, the parser must still handle them.
    List<Appender> appenders = compile("%*.*t{yyyy}");
    assertEquals(1, appenders.size());
    DefaultAppender da = assertPattern(appenders.get(0), Specifier.DATE_AND_TIME);
    assertContext(da, EnumSet.noneOf(Flag.class), FormatContext.PRECEDING, FormatContext.PRECEDING);
    assertNotNull(da.getContext().getDateTimeFormatter());
  }
}
