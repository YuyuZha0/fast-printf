package org.fastprintf;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

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
  public void test4() {
    FastPrintf fastPrintf = FastPrintf.compile("%s, %s");
    Args args = Args.of("hello".getBytes(StandardCharsets.UTF_8), "world".toCharArray());
    String format = fastPrintf.format(args);
    assertEquals("aGVsbG8=, world", format);
  }

  @Test
  // https://raw.githubusercontent.com/BartMassey/printf-tests/master/printf-tests.txt
  public void test() {
    FastPrintf fastPrintf = FastPrintf.compile("%040.1a");
    Args args = Args.of(Math.PI);
    String format = fastPrintf.format(args);
    System.out.println(format);
    System.out.printf("%040.1a%n", Math.PI);
  }

  @Test
  public void testJava() {

  }
}
