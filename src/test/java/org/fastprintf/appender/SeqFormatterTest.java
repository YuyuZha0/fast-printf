package org.fastprintf.appender;

import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SeqFormatterTest {

  private static void assertD(FormatContext ctx, int value) {
    String javaPattern = ctx.toPatternString(Specifier.SIGNED_DECIMAL_INTEGER);
    String javaResult = String.format(javaPattern, value);
    String result = SeqFormatter.d(ctx, IntForm.valueOf(value)).toString();
    assertEquals(javaPattern + ":" + value, javaResult, result);
  }

  private static void assertX(FormatContext ctx, long value) {
    String javaPattern = ctx.toPatternString(Specifier.UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE);
    String javaResult = String.format(javaPattern, value);
    String result = SeqFormatter.x(ctx, IntForm.valueOf(value)).upperCase().toString();
    assertEquals(javaPattern + ":" + value, javaResult, result);
  }

  private static void assertF(FormatContext ctx, double value) {
    String javaPattern = ctx.toPatternString(Specifier.DECIMAL_FLOATING_POINT);
    String javaResult = String.format(javaPattern, value);
    String result = SeqFormatter.f(ctx, FloatForm.valueOf(value)).toString();
    assertEquals(javaPattern + ":" + value, javaResult, result);
  }

  private static void assertE(FormatContext ctx, double value) {
    String javaPattern = ctx.toPatternString(Specifier.SCIENTIFIC_NOTATION_UPPERCASE);
    String javaResult = String.format(javaPattern, value);
    String result = SeqFormatter.e(ctx, FloatForm.valueOf(value)).upperCase().toString();
    assertEquals(javaPattern + ":" + value, javaResult, result);
  }

  private static void assertG(FormatContext ctx, double value, String expected) {
    String javaPattern = ctx.toPatternString(Specifier.USE_SHORTEST_PRESENTATION);
    String result = SeqFormatter.g(ctx, FloatForm.valueOf(value)).toString();
    assertEquals(javaPattern + ":" + value, expected, result);
  }

  private static void assertA(FormatContext ctx, double value, String expected) {
    String javaPattern = ctx.toPatternString(Specifier.HEXADECIMAL_FLOATING_POINT_UPPERCASE);
    String result = SeqFormatter.a(ctx, FloatForm.valueOf(value)).upperCase().toString();
    assertEquals(javaPattern + ":" + value, expected, result);
    if (Double.isFinite(value)) {
      double parseValue = Double.parseDouble(result);
      if (Double.isFinite(parseValue)) assertEquals(value, parseValue, 1e-8);
      int precision = result.indexOf('P') - result.indexOf('.') - 1;
      assertEquals(ctx.getPrecision(), precision);
    }
  }

  @Test
  public void testD() {

    List<String> flagsList = Arrays.asList("", "0", "- ", "+0");
    for (String flag : flagsList) {
      FormatContext ctx = FormatContext.create(flag, 12, -1);
      assertD(ctx, 0);
      assertD(ctx, 1);
      assertD(ctx, -1);
      assertD(ctx, 123);
      assertD(ctx, -123);
      assertD(ctx, 123456789);
      assertD(ctx, -123456789);
      assertD(ctx, Integer.MAX_VALUE);
      assertD(ctx, Integer.MIN_VALUE);
    }
  }

  @Test
  public void testX() {
    List<String> flagsList = Arrays.asList("", "0", "-", "#-");
    for (String flag : flagsList) {
      FormatContext ctx = FormatContext.create(flag, 20, -1);
      assertX(ctx, 1);
      assertX(ctx, -1);
      assertX(ctx, 123);
      assertX(ctx, -123);
      assertX(ctx, 123456789);
      assertX(ctx, -123456789);
      assertX(ctx, Integer.MAX_VALUE);
      assertX(ctx, Integer.MIN_VALUE);
      assertX(ctx, Long.MAX_VALUE);
      assertX(ctx, Long.MIN_VALUE);
      assertX(ctx, 0x123456789ABCDEFL);
    }
  }

  @Test
  public void testF() {
    List<String> flagsList = Arrays.asList("", "0", "-", "#-", "+0");
    for (String flag : flagsList) {
      FormatContext ctx = FormatContext.create(flag, 20, 7);
      assertF(ctx, 0.0);
      assertF(ctx, 1.0);
      assertF(ctx, -1.0);
      assertF(ctx, 123.0);
      assertF(ctx, -123.0);
      assertF(ctx, 123456789.0);
      assertF(ctx, -123456789.0);
      assertF(ctx, Float.MAX_VALUE);
      assertF(ctx, Float.MIN_VALUE);
      assertF(ctx, Double.MAX_VALUE);
      assertF(ctx, Double.MIN_VALUE);
      assertF(ctx, Math.PI);
      assertF(ctx, Math.E);
      assertF(ctx, 1 / Math.PI);
      assertF(ctx, 1 / Math.E);
      assertF(ctx, Double.NaN);
      assertF(ctx, Double.NEGATIVE_INFINITY);
      assertF(ctx, Double.POSITIVE_INFINITY);
    }
  }

  @Test
  public void testE() {
    List<String> flagsList = Arrays.asList("", "0", "-", "#-", "+0");
    for (String flag : flagsList) {
      FormatContext ctx = FormatContext.create(flag, 20, 7);
      assertE(ctx, 0.0);
      assertE(ctx, 1.0);
      assertE(ctx, -1.0);
      assertE(ctx, 123.0);
      assertE(ctx, -123.0);
      assertE(ctx, 123456789.0);
      assertE(ctx, -123456789.0);
      assertE(ctx, Float.MAX_VALUE);
      assertE(ctx, Float.MIN_VALUE);
      assertE(ctx, Double.MAX_VALUE);
      assertE(ctx, Double.MIN_VALUE);
      assertE(ctx, Math.PI);
      assertE(ctx, Math.E);
      assertE(ctx, 1 / Math.PI);
      assertE(ctx, 1 / Math.E);
      assertE(ctx, Double.NaN);
      assertE(ctx, Double.NEGATIVE_INFINITY);
      assertE(ctx, Double.POSITIVE_INFINITY);
    }
  }

  @Test
  public void testG() {
    FormatContext ctx = FormatContext.create("+", 8, 3);
    assertG(ctx, 0.0, "      +0");
    assertG(ctx, 1.0, "      +1");
    assertG(ctx, -1.0, "      -1");
    assertG(ctx, 123.0, "    +123");
    assertG(ctx, -123.0, "    -123");
    assertG(ctx, 123456789.0, "+1.23e+08");
    assertG(ctx, -123456789.0, "-1.23e+08");
    assertG(ctx, Float.MAX_VALUE, "+3.4e+38");
    assertG(ctx, Float.MIN_VALUE, "+1.4e-45");
    assertG(ctx, Double.MAX_VALUE, "+1.8e+308");
    assertG(ctx, Double.MIN_VALUE, "+4.9e-324");
    assertG(ctx, Math.PI, "   +3.14");
    assertG(ctx, Math.E, "   +2.72");
    assertG(ctx, Double.NaN, "     NaN");
    assertG(ctx, Double.NEGATIVE_INFINITY, "-Infinity");
    assertG(ctx, Double.POSITIVE_INFINITY, "+Infinity");
  }

  @Test
  public void testA() {
    FormatContext ctx = FormatContext.create("", -1, 8);
    assertA(ctx, 0.0, "0X0.00000000P+0");
    assertA(ctx, 1.0, "0X1.00000000P+0");
    assertA(ctx, -1.0, "-0X1.00000000P+0");
    assertA(ctx, 123.0, "0X1.EC000000P+6");
    assertA(ctx, -123.0, "-0X1.EC000000P+6");
    assertA(ctx, 123456789.0, "0X1.D6F34540P+26");
    assertA(ctx, -123456789.0, "-0X1.D6F34540P+26");
    assertA(ctx, Float.MAX_VALUE, "0X1.FFFFFE00P+127");
    assertA(ctx, Float.MIN_VALUE, "0X1.00000000P-149");
    assertA(ctx, Double.MAX_VALUE, "0X1.00000000P+1024");
    assertA(ctx, Double.MIN_VALUE, "0X1.00000000P-1074");
    assertA(ctx, Math.PI, "0X1.921FB544P+1");
    assertA(ctx, Math.E, "0X1.5BF0A8B1P+1");
    assertA(ctx, 1 / Math.PI, "0X1.45F306DDP-2");
    assertA(ctx, 1 / Math.E, "0X1.78B56363P-2");
    assertA(ctx, Double.NaN, "NAN");
    assertA(ctx, Double.NEGATIVE_INFINITY, "-INFINITY");
    assertA(ctx, Double.POSITIVE_INFINITY, "INFINITY");
  }
}
