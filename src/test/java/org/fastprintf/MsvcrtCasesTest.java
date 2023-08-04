package org.fastprintf;

import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;

// https://github.com/BartMassey/printf-tests/blob/master/sources/tests-msvcrt-printf.c
public class MsvcrtCasesTest {

  private static void assertFormatResult(String pattern, String expected, Object... value) {
    FastPrintf fastPrintf = FastPrintf.compile(pattern);
    Args args = Args.of(value);
    String format;
    try (Writer writer = new StringWriter()) {
      fastPrintf.format(writer, args);
      format = writer.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    assertEquals(pattern, expected, format);
  }

  @Test
  public void test() {
    Double pnumber = 789456123D;
    assertFormatResult("%+#23.15e", " +7.894561230000000e+08", pnumber);
    assertFormatResult("%-#23.15e", "7.894561230000000e+08  ", pnumber);
    assertFormatResult("%#23.15e", "  7.894561230000000e+08", pnumber);
    assertFormatResult("%#1.1g", "8e+08", pnumber);
    assertFormatResult("% d", " 1", 1);
    assertFormatResult("%+d", "+1", 1);
    assertFormatResult("% +d", "+1", 1);
    assertFormatResult("%S", "WIDE", "wide");
    assertFormatResult("%#012x", "0x0000000001", 1);
    assertFormatResult("%#04.8x", "0x00000001", 1);
    assertFormatResult("%#-08.2x", "0x01    ", 1);
    assertFormatResult("%#08o", "00000001", 1);
    assertFormatResult("%.1s", "f", "foo");
    assertFormatResult("%.*s", "f", 1, "foo");
    assertFormatResult("%3c", "  a", 'a');
    assertFormatResult("%%0", "%0", 'a');
    assertFormatResult("%%0%n%s", "%0bar", "foo", "bar");
  }
}
