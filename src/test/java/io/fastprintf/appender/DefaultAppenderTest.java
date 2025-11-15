package io.fastprintf.appender;

import static org.junit.Assert.*;

import io.fastprintf.Flag;
import io.fastprintf.FormatContext;
import io.fastprintf.PrintfException;
import io.fastprintf.Specifier;
import io.fastprintf.seq.Seq;
import io.fastprintf.traits.CharSequenceTraits;
import io.fastprintf.traits.DoubleTraits;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.IntTraits;
import io.fastprintf.traits.TemporalAccessorTraits;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import org.junit.Test;

public class DefaultAppenderTest {

  /** Helper to format values using a default FormatContext. */
  private static String formatValue(Specifier specifier, FormatTraits... traits) {
    return formatValue(specifier, FormatContext.create(""), traits);
  }

  /**
   * Main helper to format values using a given Specifier and FormatContext. This encapsulates the
   * boilerplate of setting up the appender and its dependencies.
   */
  private static String formatValue(
      Specifier specifier, FormatContext context, FormatTraits... traits) {
    DefaultAppender appender = new DefaultAppender(specifier, context);
    Iterator<FormatTraits> iterator = Arrays.asList(traits).iterator();
    final StringBuilder resultBuilder = new StringBuilder();
    Consumer<Seq> consumer =
        new Consumer<Seq>() {
          @Override
          public void accept(Seq seq) {
            resultBuilder.append(seq.toString());
          }
        };

    appender.append(consumer, iterator);

    return resultBuilder.toString();
  }

  @Test
  public void testConstructorAndGetters() {
    FormatContext ctx = FormatContext.create("", 10, 5);
    Specifier spec = Specifier.SIGNED_DECIMAL_INTEGER;
    DefaultAppender appender = new DefaultAppender(spec, ctx);

    assertNotNull("Appender should not be null", appender);
    assertEquals(
        "getSpecifier() should return the correct specifier", spec, appender.getSpecifier());
    assertEquals("getContext() should return the correct context", ctx, appender.getContext());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_NullSpecifier() {
    new DefaultAppender(null, FormatContext.create(""));
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_NullContext() {
    new DefaultAppender(Specifier.STRING, null);
  }

  @Test
  public void testToString_ReconstructsPattern() {
    FormatContext ctx = FormatContext.create("-#0", 12, 5);
    DefaultAppender appender = new DefaultAppender(Specifier.UNSIGNED_HEXADECIMAL_INTEGER, ctx);
    // The order of flags in the output string is not guaranteed, so we check for presence
    String pattern = appender.toString();
    assertTrue(pattern.startsWith("%"));
    assertTrue(pattern.contains("-"));
    assertTrue(pattern.contains("#"));
    assertTrue(pattern.contains("0"));
    assertTrue(pattern.contains("12"));
    assertTrue(pattern.contains(".5"));
    assertTrue(pattern.endsWith("x"));
  }

  @Test
  public void testAppend_SimpleCase() {
    FormatContext ctx = FormatContext.create("", 5, -1);
    DefaultAppender appender = new DefaultAppender(Specifier.SIGNED_DECIMAL_INTEGER, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Collections.singletonList(IntTraits.ofPrimitive(42));

    appender.append(collect::add, traits.iterator());

    assertEquals("Should have one sequence in the list", 1, collect.size());
    assertEquals("   42", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingWidth() {
    FormatContext ctx = FormatContext.create("", FormatContext.PRECEDING, -1);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits =
        Arrays.asList(IntTraits.ofPrimitive(10), new CharSequenceTraits("test"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("      test", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingWidth_NegativeForLeftJustify() {
    FormatContext ctx = FormatContext.create("", FormatContext.PRECEDING, -1);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits =
        Arrays.asList(IntTraits.ofPrimitive(-10), new CharSequenceTraits("test"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("test      ", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingPrecision() {
    FormatContext ctx = FormatContext.create("", -1, FormatContext.PRECEDING);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits =
        Arrays.asList(IntTraits.ofPrimitive(3), new CharSequenceTraits("hello"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("hel", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingPrecision_NegativeIsUnset() {
    FormatContext ctx = FormatContext.create("", -1, FormatContext.PRECEDING);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits =
        Arrays.asList(IntTraits.ofPrimitive(-5), new CharSequenceTraits("hello"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("hello", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingWidthAndPrecision() {
    FormatContext ctx = FormatContext.create("", FormatContext.PRECEDING, FormatContext.PRECEDING);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits =
        Arrays.asList(
            IntTraits.ofPrimitive(10), IntTraits.ofPrimitive(3), new CharSequenceTraits("hello"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("       hel", collect.get(0).toString());
  }

  @Test(expected = PrintfException.class)
  public void testAppend_MissingArgumentForSpecifier() {
    DefaultAppender appender =
        new DefaultAppender(Specifier.SIGNED_DECIMAL_INTEGER, FormatContext.create(""));
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Collections.emptyList(); // No arguments provided

    appender.append(collect::add, traits.iterator());
  }

  @Test(expected = PrintfException.class)
  public void testAppend_MissingArgumentForWidth() {
    DefaultAppender appender =
        new DefaultAppender(
            Specifier.SIGNED_DECIMAL_INTEGER,
            FormatContext.create("", FormatContext.PRECEDING, -1));
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Collections.emptyList(); // Missing width argument

    appender.append(collect::add, traits.iterator());
  }

  @Test(expected = PrintfException.class)
  public void testAppend_MissingArgumentForPrecision() {
    DefaultAppender appender =
        new DefaultAppender(
            Specifier.SIGNED_DECIMAL_INTEGER,
            FormatContext.create("", -1, FormatContext.PRECEDING));
    List<Seq> collect = new ArrayList<>();
    // Missing precision argument, but has main argument
    List<FormatTraits> traits = Collections.singletonList(IntTraits.ofPrimitive(123));

    appender.append(collect::add, traits.iterator());
  }

  @Test
  public void testAllSpecifiersAreHandled() {
    // This test ensures that formatterForSpecifier does not throw an exception
    // for any known specifier.
    for (Specifier spec : Specifier.values()) {
      try {
        DefaultAppender appender = new DefaultAppender(spec, FormatContext.create(""));
        assertNotNull(appender); // Just a simple check to use the variable
      } catch (Exception e) {
        fail("formatterForSpecifier failed for specifier: " + spec);
      }
    }
  }

  @Test
  public void testAppend_unsignedHexUppercase() {
    String result =
        formatValue(Specifier.UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE, IntTraits.ofPrimitive(255));
    assertEquals("FF", result);
  }

  @Test
  public void testAppend_decimalFloatUppercase() {
    assertEquals(
        "INFINITY",
        formatValue(
            Specifier.DECIMAL_FLOATING_POINT_UPPERCASE,
            DoubleTraits.ofPrimitive(Double.POSITIVE_INFINITY)));
    assertEquals(
        "NAN",
        formatValue(
            Specifier.DECIMAL_FLOATING_POINT_UPPERCASE, DoubleTraits.ofPrimitive(Double.NaN)));
  }

  @Test
  public void testAppend_scientificUppercase() {
    // Default precision is 6. %e for 123.456 is 1.234560e+02
    String result =
        formatValue(Specifier.SCIENTIFIC_NOTATION_UPPERCASE, DoubleTraits.ofPrimitive(123.456));
    assertEquals("1.234560E+02", result);
  }

  @Test
  public void testAppend_hexFloatUppercase() {
    // %a for 1.0 is 0x1.0p0
    String result =
        formatValue(Specifier.HEXADECIMAL_FLOATING_POINT_UPPERCASE, DoubleTraits.ofPrimitive(1.0));
    assertEquals("0X1.0P+0", result);
  }

  @Test
  public void testAppend_stringUppercase() {
    String result = formatValue(Specifier.STRING_UPPERCASE, new CharSequenceTraits("Hello World"));
    assertEquals("HELLO WORLD", result);
  }

  @Test
  public void testAppend_dateTimeUppercase() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 10, 0);
    // Specify a fixed Locale to make the test deterministic.
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US);

    // CORRECTED: Use the primary factory method with an empty EnumSet for the flags.
    FormatContext context = FormatContext.create(EnumSet.noneOf(Flag.class), -1, -1, dtf);

    // With Locale.US, %t using this format would be "27-Oct-2023".
    // The .upperCase() call from the %T specifier should turn this into "27-OCT-2023".
    String result =
        formatValue(
            Specifier.DATE_AND_TIME_UPPERCASE, context, new TemporalAccessorTraits(dateTime));
    assertEquals("27-OCT-2023", result);
  }

  @Test
  public void testAppend_shortestPresentationUppercase() {
    assertEquals(
        "1.23457E+30",
        formatValue(
            Specifier.USE_SHORTEST_PRESENTATION_UPPERCASE,
            DoubleTraits.ofPrimitive(1.23456789e30)));
    assertEquals(
        "1.2E-10",
        formatValue(
            Specifier.USE_SHORTEST_PRESENTATION_UPPERCASE,
            FormatContext.create("", -1, 2),
            DoubleTraits.ofPrimitive(1.23e-10)));
    assertEquals(
        "INFINITY",
        formatValue(
            Specifier.USE_SHORTEST_PRESENTATION_UPPERCASE,
            DoubleTraits.ofPrimitive(Double.POSITIVE_INFINITY)));
  }

  // --- Helper Methods ---

  @Test
  public void testAppend_nothingPrinted() {
    // The %n specifier should produce no output, but DefaultAppender still consumes one argument.
    String result = formatValue(Specifier.NOTHING_PRINTED, IntTraits.ofPrimitive(123));
    assertEquals("", result);
  }

  @Test
  public void testAppend_percentSign() {
    // Verifies the "dead code" branch for completeness.
    // DefaultAppender will consume an argument even if the formatter lambda ignores it.
    String result = formatValue(Specifier.PERCENT_SIGN, new CharSequenceTraits("ignored"));
    assertEquals("%", result);
  }
}
