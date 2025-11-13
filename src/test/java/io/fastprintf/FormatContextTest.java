package io.fastprintf;

import static org.junit.Assert.*;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import org.junit.Test;

public class FormatContextTest {

  private static final int MAX_ALLOWED = 65536;

  // --- Creation and Validation Tests ---

  @Test
  public void testCreate_validValues() {
    FormatContext ctx = FormatContext.create(EnumSet.noneOf(Flag.class), 10, 5, null);
    assertEquals(10, ctx.getWidth());
    assertEquals(5, ctx.getPrecision());
    assertTrue(ctx.getFlags().isEmpty());
  }

  @Test
  public void testCreate_validBoundaryValues() {
    FormatContext.create(EnumSet.noneOf(Flag.class), 0, 0, null);
    FormatContext.create(EnumSet.noneOf(Flag.class), MAX_ALLOWED, MAX_ALLOWED, null);
    FormatContext.create(
        EnumSet.noneOf(Flag.class), FormatContext.UNSET, FormatContext.UNSET, null);
    FormatContext.create(
        EnumSet.noneOf(Flag.class), FormatContext.PRECEDING, FormatContext.PRECEDING, null);
  }

  @Test(expected = PrintfException.class)
  public void testCreate_throwsOnNegativeWidth() {
    FormatContext.create(EnumSet.noneOf(Flag.class), -2, 5, null);
  }

  @Test(expected = PrintfException.class)
  public void testCreate_throwsOnNegativePrecision() {
    FormatContext.create(EnumSet.noneOf(Flag.class), 10, -2, null);
  }

  @Test(expected = PrintfException.class)
  public void testCreate_throwsOnTooLargeWidth() {
    FormatContext.create(EnumSet.noneOf(Flag.class), MAX_ALLOWED + 1, 5, null);
  }

  @Test(expected = PrintfException.class)
  public void testCreate_throwsOnTooLargePrecision() {
    FormatContext.create(EnumSet.noneOf(Flag.class), 10, MAX_ALLOWED + 1, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCreate_throwsOnNullFlags() {
    FormatContext.create(null, 10, 5, null);
  }

  // --- Immutability Tests ---

  @Test
  public void testImmutability_setWidth() {
    FormatContext ctx1 = FormatContext.create("", 10, 5);
    FormatContext ctx2 = ctx1.setWidth(20);
    assertNotSame(ctx1, ctx2);
    assertEquals(10, ctx1.getWidth()); // Original is unchanged
    assertEquals(20, ctx2.getWidth()); // New one has the new value
    assertEquals(5, ctx2.getPrecision()); // Other fields are copied
  }

  @Test
  public void testImmutability_setPrecision() {
    FormatContext ctx1 = FormatContext.create("", 10, 5);
    FormatContext ctx2 = ctx1.setPrecision(8);
    assertNotSame(ctx1, ctx2);
    assertEquals(5, ctx1.getPrecision());
    assertEquals(8, ctx2.getPrecision());
    assertEquals(10, ctx2.getWidth());
  }

  @Test
  public void testImmutability_addFlag() {
    FormatContext ctx1 = FormatContext.create("", 10, 5);
    FormatContext ctx2 = ctx1.addFlag(Flag.ALTERNATE);
    assertNotSame(ctx1, ctx2);
    assertFalse(ctx1.hasFlag(Flag.ALTERNATE)); // Original is unchanged
    assertTrue(ctx2.hasFlag(Flag.ALTERNATE)); // New one has the flag
  }

  @Test
  public void addFlag_returnsSameInstanceIfFlagExists() {
    FormatContext ctx1 = FormatContext.create("#", 10, 5);
    FormatContext ctx2 = ctx1.addFlag(Flag.ALTERNATE);
    assertSame("Should return same instance if flag is already present", ctx1, ctx2);
  }

  @Test
  public void addFlag_returnsSameInstanceIfFlagIsNull() {
    FormatContext ctx1 = FormatContext.create("", 10, 5);
    FormatContext ctx2 = ctx1.addFlag(null);
    assertSame("Should return same instance if flag is null", ctx1, ctx2);
  }

  // --- Helper and Boolean Method Tests ---

  @Test
  public void testBooleanChecks() {
    FormatContext ctx = FormatContext.create("#-", 10, 5);
    assertTrue(ctx.isWidthSet());
    assertTrue(ctx.isPrecisionSet());
    assertFalse(ctx.isPrecedingWidth());
    assertFalse(ctx.isPrecedingPrecision());
    assertTrue(ctx.hasFlag(Flag.ALTERNATE));
    assertTrue(ctx.hasFlag(Flag.LEFT_JUSTIFY));
    assertFalse(ctx.hasFlag(Flag.PLUS));
    assertFalse(ctx.hasFlag(null));

    FormatContext ctx2 = FormatContext.create("", FormatContext.UNSET, FormatContext.UNSET);
    assertFalse(ctx2.isWidthSet());
    assertFalse(ctx2.isPrecisionSet());

    FormatContext ctx3 = FormatContext.create("", FormatContext.PRECEDING, FormatContext.PRECEDING);
    assertTrue(ctx3.isPrecedingWidth());
    assertTrue(ctx3.isPrecedingPrecision());
  }

  @Test
  public void testGetDateTimeFormatter() {
    DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE;
    FormatContext ctx = FormatContext.create(EnumSet.noneOf(Flag.class), -1, -1, dtf);
    assertSame(dtf, ctx.getDateTimeFormatter());
  }

  // --- toString and toPatternString Tests ---

  @Test
  public void testToPatternString() {
    assertEquals("%d", FormatContext.create("").toPatternString(Specifier.SIGNED_DECIMAL_INTEGER));
    assertEquals(
        "%10d", FormatContext.create("", 10, -1).toPatternString(Specifier.SIGNED_DECIMAL_INTEGER));
    assertEquals("%.5s", FormatContext.create("", -1, 5).toPatternString(Specifier.STRING));
    assertEquals(
        "%-10.5f",
        FormatContext.create("-", 10, 5).toPatternString(Specifier.DECIMAL_FLOATING_POINT));
    assertEquals(
        "%#+ 0x",
        FormatContext.create("#0+ ", -1, -1)
            .toPatternString(Specifier.UNSIGNED_HEXADECIMAL_INTEGER));
    assertEquals(
        "%*.*g",
        FormatContext.create("", FormatContext.PRECEDING, FormatContext.PRECEDING)
            .toPatternString(Specifier.USE_SHORTEST_PRESENTATION));
    assertEquals(
        "%*s",
        FormatContext.create("", FormatContext.PRECEDING, -1).toPatternString(Specifier.STRING));
    assertEquals(
        "%.*c",
        FormatContext.create("", -1, FormatContext.PRECEDING).toPatternString(Specifier.CHARACTER));
  }

  @Test
  public void testToString_doesNotThrow() {
    FormatContext ctx = FormatContext.create("#+- 0", 123, 456);
    String s = ctx.toString();
    assertTrue(s.contains("flags="));
    assertTrue(s.contains("width=123"));
    assertTrue(s.contains("precision=456"));
  }
}
