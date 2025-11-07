package io.fastprintf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.fastprintf.appender.Appender;
import io.fastprintf.appender.DefaultAppender;
import io.fastprintf.appender.FixedStringAppender;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

public class CompilerTest {

  private void assertFixed(Appender appender, String value) {
    assertTrue(appender instanceof FixedStringAppender);
    assertEquals(value, ((FixedStringAppender) appender).getValue());
  }

  private void assertPattern(
      Appender appender, Specifier specifier, EnumSet<Flag> flags, int width, int precision) {
    assertTrue(appender instanceof DefaultAppender);
    DefaultAppender defaultAppender = (DefaultAppender) appender;
    assertEquals(specifier, defaultAppender.getSpecifier());
    FormatContext context = defaultAppender.getContext();
    assertEquals(flags, context.getFlags());
    assertEquals(width, context.getWidth());
    assertEquals(precision, context.getPrecision());
  }

  @Test
  public void test1() {
    Compiler compiler = new Compiler("Characters: %c %c \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(5, appenders.size());
    assertFixed(appenders.get(0), "Characters: ");
    assertPattern(appenders.get(1), Specifier.CHARACTER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(2), " ");
    assertPattern(appenders.get(3), Specifier.CHARACTER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(4), " \n");
  }

  @Test
  public void test2() {
    Compiler compiler = new Compiler("Decimals: %d %d\n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(5, appenders.size());
    assertFixed(appenders.get(0), "Decimals: ");
    assertPattern(
        appenders.get(1), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(2), " ");
    assertPattern(
        appenders.get(3), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(4), "\n");
  }

  @Test
  public void test3() {
    Compiler compiler = new Compiler("Preceding with zeros: %010d \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(3, appenders.size());
    assertFixed(appenders.get(0), "Preceding with zeros: ");
    assertPattern(
        appenders.get(1), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.of(Flag.ZERO_PAD), 10, -1);
    assertFixed(appenders.get(2), " \n");
  }

  @Test
  public void test4() {
    Compiler compiler = new Compiler("Some different radices: %d %x %o %#x %#o \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(11, appenders.size());
    assertFixed(appenders.get(0), "Some different radices: ");
    assertPattern(
        appenders.get(1), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(2), " ");
    assertPattern(
        appenders.get(3),
        Specifier.UNSIGNED_HEXADECIMAL_INTEGER,
        EnumSet.noneOf(Flag.class),
        -1,
        -1);
    assertFixed(appenders.get(4), " ");
    assertPattern(
        appenders.get(5), Specifier.UNSIGNED_OCTAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(6), " ");
    assertPattern(
        appenders.get(7),
        Specifier.UNSIGNED_HEXADECIMAL_INTEGER,
        EnumSet.of(Flag.ALTERNATE),
        -1,
        -1);
    assertFixed(appenders.get(8), " ");
    assertPattern(
        appenders.get(9), Specifier.UNSIGNED_OCTAL_INTEGER, EnumSet.of(Flag.ALTERNATE), -1, -1);
    assertFixed(appenders.get(10), " \n");
  }

  @Test
  public void test5() {
    Compiler compiler = new Compiler("floats: %4.2f %+.0e %E \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(7, appenders.size());
    assertFixed(appenders.get(0), "floats: ");
    assertPattern(
        appenders.get(1), Specifier.DECIMAL_FLOATING_POINT, EnumSet.noneOf(Flag.class), 4, 2);
    assertFixed(appenders.get(2), " ");
    assertPattern(appenders.get(3), Specifier.SCIENTIFIC_NOTATION, EnumSet.of(Flag.PLUS), -1, 0);
    assertFixed(appenders.get(4), " ");
    assertPattern(
        appenders.get(5),
        Specifier.SCIENTIFIC_NOTATION_UPPERCASE,
        EnumSet.noneOf(Flag.class),
        -1,
        -1);
    assertFixed(appenders.get(6), " \n");
  }

  @Test
  public void test6() {
    Compiler compiler = new Compiler("Width trick: %*d \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertFixed(appenders.get(0), "Width trick: ");
    assertPattern(
        appenders.get(1),
        Specifier.SIGNED_DECIMAL_INTEGER,
        EnumSet.noneOf(Flag.class),
        Integer.MIN_VALUE,
        -1);
    assertFixed(appenders.get(2), " \n");
  }

  @Test
  public void test7() {
    Compiler compiler = new Compiler("%%\u0025\u0025");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(2, appenders.size());
    assertFixed(appenders.get(0), "%");
    assertFixed(appenders.get(1), "%");
  }

  @Test
  public void test8() {
    Compiler compiler = new Compiler("Date time: %16{yyyy-MM-dd HH:mm:ss}t");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(2, appenders.size());
    assertFixed(appenders.get(0), "Date time: ");
    Appender appender = appenders.get(1);
    assertTrue(appender instanceof DefaultAppender);
    DefaultAppender defaultAppender = (DefaultAppender) appender;
    assertEquals(Specifier.DATE_AND_TIME, defaultAppender.getSpecifier());
    FormatContext context = defaultAppender.getContext();
    assertEquals(EnumSet.noneOf(Flag.class), context.getFlags());
    assertEquals(16, context.getWidth());
    assertEquals(FormatContext.UNSET, context.getPrecision());
    assertEquals(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").toString(),
        context.getDateTimeFormatter().toString());
  }

  @Test
  public void testDateTimePatternParsing() {
    // Valid simple pattern
    Compiler compiler1 = new Compiler("%{yyyy-MM-dd}t");
    compiler1.compile();
    DefaultAppender appender1 = (DefaultAppender) compiler1.getAppenders().get(0);
    DateTimeFormatter dtf1 = appender1.getContext().getDateTimeFormatter();
    assertNotNull(dtf1);
    // The string representation of DateTimeFormatter can vary between JDK versions.
    // Instead of comparing strings, we check for a known substring.
    String dtfString = dtf1.toString();
    assertTrue(dtfString.contains("Year") || dtfString.contains("YearOfEra"));
    assertTrue(dtfString.contains("MonthOfYear"));
    assertTrue(dtfString.contains("DayOfMonth"));

    // Valid pattern with quoted literals (single quotes)
    Compiler compiler2 = new Compiler("%{'Date is 'yyyy-MM-dd}t");
    compiler2.compile();
    DefaultAppender appender2 = (DefaultAppender) compiler2.getAppenders().get(0);
    DateTimeFormatter dtf2 = appender2.getContext().getDateTimeFormatter();
    assertNotNull(dtf2);
    assertTrue(dtf2.toString().startsWith("'Date is '"));
  }

  @Test(expected = PrintfSyntaxException.class)
  public void testDateTimePatternUnclosedBrace() {
    // This should fail because the closing brace is missing
    Compiler compiler = new Compiler("%{yyyy-MM-dd");
    compiler.compile();
  }

  @Test(expected = PrintfSyntaxException.class)
  public void testDateTimePatternInvalidPatternLetter() {
    // This should fail because "J" is not a valid DateTimeFormatter pattern letter.
    Compiler compiler = new Compiler("%{J}t");
    compiler.compile();
  }

  @Test
  public void testDateTimePatternWithNestedBraceSucceeds() {
    // This test validates that the robust parser correctly handles a literal
    // (and quoted) brace inside the pattern.
    Compiler compiler = new Compiler("%{yyyy-MM-dd'T'HH:mm:ss'}'}t");
    compiler.compile();
    DefaultAppender appender = (DefaultAppender) compiler.getAppenders().get(0);
    assertNotNull(appender.getContext().getDateTimeFormatter());
    // Verify by formatting a date with the resulting formatter
    String formatted =
        appender
            .getContext()
            .getDateTimeFormatter()
            .format(java.time.LocalDate.of(2023, 1, 1).atStartOfDay());
    assertEquals("2023-01-01T00:00:00}", formatted);
  }

  @Test
  public void testDateTimePatternIsParsedCorrectly() {
    // This test verifies that the compiler associates a date/time pattern
    // with the IMMEDIATE next specifier ('d' in this case), and treats
    // the rest of the string as a literal.
    Compiler compiler = new Compiler("%{MM}d literal text");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(2, appenders.size());

    // Check the first appender: should be a DefaultAppender for %{MM}d
    assertTrue(appenders.get(0) instanceof DefaultAppender);
    DefaultAppender dateAppender = (DefaultAppender) appenders.get(0);
    assertEquals(Specifier.SIGNED_DECIMAL_INTEGER, dateAppender.getSpecifier());
    assertNotNull(dateAppender.getContext().getDateTimeFormatter());
    assertEquals(
        DateTimeFormatter.ofPattern("MM").toString(),
        dateAppender.getContext().getDateTimeFormatter().toString());

    // Check the subsequent fixed string
    assertFixed(appenders.get(1), " literal text");
  }

  @Test
  public void testCompilerErrorHandlingAndEdgeCases() {
    // 1. Incomplete/Truncated Specifiers
    try {
      new Compiler("%").compile();
      org.junit.Assert.fail("Should throw on trailing '%'");
    } catch (PrintfSyntaxException e) {
      assertEquals(
          "Format string terminates in the middle of a format specifier", e.getDescription());
    }

    try {
      new Compiler("hello %10").compile();
      org.junit.Assert.fail("Should throw on string ending after width");
    } catch (PrintfSyntaxException e) {
      assertEquals("Format string lacks specifier", e.getDescription());
    }

    try {
      new Compiler("%10.").compile();
      org.junit.Assert.fail("Should throw on string ending after precision dot");
    } catch (PrintfSyntaxException e) {
      assertEquals("Format string terminates after '.'", e.getDescription());
    }

    // 2. Invalid Flags, Width, Precision
    try {
      new Compiler("%--d").compile();
      org.junit.Assert.fail("Should throw on duplicate flag");
    } catch (PrintfSyntaxException e) {
      assertTrue(e.getDescription().contains("Duplicate flag"));
    }

    try {
      // A width larger than Integer.MAX_VALUE
      new Compiler("%99999999999d").compile();
      org.junit.Assert.fail("Should throw on invalid width (integer overflow)");
    } catch (PrintfSyntaxException e) {
      assertEquals("Invalid width: '99999999999'", e.getDescription());
    }

    // 3. Precision Edge Cases
    // A format with just a dot for precision should be treated as precision 0.
    Compiler compiler = new Compiler("%.s");
    compiler.compile();
    DefaultAppender appender = (DefaultAppender) compiler.getAppenders().get(0);
    assertEquals(0, appender.getContext().getPrecision());

    // 4. Date/Time Pattern Edge Cases
    try {
      new Compiler("%{}t").compile();
      org.junit.Assert.fail("Should throw on empty date/time pattern");
    } catch (PrintfSyntaxException e) {
      assertEquals("Empty date/time pattern", e.getDescription());
    }

    // 5. Ignored Flags/Precision for certain specifiers (should compile successfully)
    new Compiler("%#s").compile(); // '#' flag is ignored for strings
    new Compiler("%+c").compile(); // '+' flag is ignored for chars
    new Compiler("%.5c").compile(); // precision is ignored for chars
  }
}
