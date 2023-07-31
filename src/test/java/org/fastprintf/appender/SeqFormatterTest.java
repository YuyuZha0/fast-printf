package org.fastprintf.appender;

import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.box.IntFamily;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SeqFormatterTest {

  private static void assertD(FormatContext ctx, int value) {
    String javaPattern = ctx.toPatternString(Specifier.SIGNED_DECIMAL_INTEGER);
    String javaResult = String.format(javaPattern, value);
    String result = SeqFormatter.d(ctx, IntFamily.valueOf(value)).toString();
    assertEquals(javaPattern + ":" + value, javaResult, result);
  }

  private static void assertX(FormatContext ctx, long value) {
    String javaPattern = ctx.toPatternString(Specifier.UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE);
    String javaResult = String.format(javaPattern, value);
    String result = SeqFormatter.x(ctx, IntFamily.valueOf(value)).upperCase().toString();
    assertEquals(javaPattern + ":" + value, javaResult, result);
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
}
