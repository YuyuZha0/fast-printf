package io.fastprintf;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class BigDecimalCasesTest {

  private static void assertFormatResult(String pattern, String expected, double d) {
    FastPrintf fastPrintf = FastPrintf.compile(pattern);
    Args args = Args.of(BigDecimal.valueOf(d));
    String format = fastPrintf.format(args);
    assertEquals(pattern, expected, format);
  }

  @Test
  public void test() {
    assertFormatResult("%d", "42", 42.8952);
    assertFormatResult("%.2d", "42", 42.8952);
    assertFormatResult("%.2i", "42", 42.8952);
    assertFormatResult("%.2f", "42.90", 42.8952);
    assertFormatResult("%.2F", "42.90", 42.8952);
    assertFormatResult("%.10f", "42.8952000000", 42.8952);
    assertFormatResult("%1.2f", "42.90", 42.8952);
    assertFormatResult("%6.2f", " 42.90", 42.8952);
    assertFormatResult("%06.2f", "042.90", 42.8952);
    assertFormatResult("%+6.2f", "+42.90", 42.8952);
    assertFormatResult("%5.10f", "42.8952000000", 42.8952);

    double pnumber = 789456123D;
    assertFormatResult("%+#23.15e", " +7.894561230000000e+08", pnumber);
    assertFormatResult("%-#23.15e", "7.894561230000000e+08  ", pnumber);
    assertFormatResult("%#23.15e", "  7.894561230000000e+08", pnumber);
    assertFormatResult("%#1.1g", "8e+08", pnumber);
  }
}
