package io.fastprintf.appender;

import static org.junit.Assert.*;

import io.fastprintf.FormatContext;
import io.fastprintf.PrintfException;
import io.fastprintf.Specifier;
import io.fastprintf.seq.Seq;
import io.fastprintf.traits.CharSequenceTraits;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.IntTraits;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class DefaultAppenderTest {

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
    List<FormatTraits> traits = Collections.singletonList(new IntTraits(42));

    appender.append(collect::add, traits.iterator());

    assertEquals("Should have one sequence in the list", 1, collect.size());
    assertEquals("   42", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingWidth() {
    FormatContext ctx = FormatContext.create("", FormatContext.PRECEDING, -1);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Arrays.asList(new IntTraits(10), new CharSequenceTraits("test"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("      test", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingWidth_NegativeForLeftJustify() {
    FormatContext ctx = FormatContext.create("", FormatContext.PRECEDING, -1);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Arrays.asList(new IntTraits(-10), new CharSequenceTraits("test"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("test      ", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingPrecision() {
    FormatContext ctx = FormatContext.create("", -1, FormatContext.PRECEDING);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Arrays.asList(new IntTraits(3), new CharSequenceTraits("hello"));

    appender.append(collect::add, traits.iterator());

    assertEquals(1, collect.size());
    assertEquals("hel", collect.get(0).toString());
  }

  @Test
  public void testAppend_PrecedingPrecision_NegativeIsUnset() {
    FormatContext ctx = FormatContext.create("", -1, FormatContext.PRECEDING);
    DefaultAppender appender = new DefaultAppender(Specifier.STRING, ctx);
    List<Seq> collect = new ArrayList<>();
    List<FormatTraits> traits = Arrays.asList(new IntTraits(-5), new CharSequenceTraits("hello"));

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
        Arrays.asList(new IntTraits(10), new IntTraits(3), new CharSequenceTraits("hello"));

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
    List<FormatTraits> traits = Collections.singletonList(new IntTraits(123));

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
}
