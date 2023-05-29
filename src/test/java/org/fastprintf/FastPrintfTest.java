package org.fastprintf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FastPrintfTest {

  @Test
  public void test1() {
    FastPrintf fastPrintf = FastPrintf.compile("%d %f %s");
    Args args = Args.of(1, 2.0, "hello");
    String format = fastPrintf.format(args);
    assertEquals("1 2.000000 hello", format);
  }

  @Test
  public void test2() {
    FastPrintf fastPrintf = FastPrintf.compile("Some different radices: %d %x %o %#x %#o ");
    Args args = Args.of(1, 2, 3, 4, 5);
    String format = fastPrintf.format(args);
    assertEquals("Some different radices: 1 2 3 0x4 05 ", format);
  }

  @Test
  public void test3() {
    FastPrintf fastPrintf = FastPrintf.compile("floats: %4.2f %+.0e %E \n");
    Args args = Args.of(3.1416, 3.1416, 3.1416);
    String format = fastPrintf.format(args);
    assertEquals("floats: 3.14 +3e+00 3.141600E+00 \n", format);
  }

  @Test
  // https://raw.githubusercontent.com/BartMassey/printf-tests/master/printf-tests.txt
  public void test() {
    FastPrintf fastPrintf =
        FastPrintf.compile("%d %i %u %o %x %X %f %F %e %E %g %G %a %A %c %s %i %%");
    Args args = Args.of(1, 2, 3, 4, 5, 6, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14, "hello", 15);
    String format = fastPrintf.format(args);
    assertEquals(
        "1 2 3 4 5 6 7.000000 8.000000 9.000000e+00 1.000000E+01 11.000000 12.000000 0xd.0p+3 0XD.0P+3 \nhello 15 %",
        format);
  }
}
