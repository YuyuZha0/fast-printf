package org.fastprintf;

import org.fastprintf.traits.FormatTraits;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class FastPrintfTest {

  @Test
  public void test1() {
    FastPrintf fastPrintf = FastPrintf.compile("%d %f %s %s");
    Args args = Args.of(1, 2.0, "hello", null);
    String format = fastPrintf.format(args);
    assertEquals("1 2.000000 hello null", format);
  }

  @Test
  public void test2() {
    FastPrintf fastPrintf = FastPrintf.compile("Some different radices: %d %x %o %#x %#o %X");
    Args args = Args.of(1, 2, 3, 4, 5, null);
    String format = fastPrintf.format(args);
    assertEquals("Some different radices: 1 2 3 0x4 05 null", format);
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
  public void test5() {
    FastPrintf fastPrintf = FastPrintf.compile("%p, %p, %p, %p, %p, %p, %p, %p, %p, %p, %p");
    Args args =
        Args.of(
            1,
            1L,
            1.0,
            1.0f,
            'a',
            true,
            "hello",
            new int[0],
            new Object[0],
            (short) 9,
            new ArrayList<>());
    String format = fastPrintf.format(args);
    System.out.println(format);
  }

  @Test(expected = PrintfException.class)
  // https://raw.githubusercontent.com/BartMassey/printf-tests/master/printf-tests.txt
  public void testException() {
    BigDecimal bigDecimal = new BigDecimal("3.7415926");
    FastPrintf fastPrintf = FastPrintf.compile("%a");
    Args args = Args.of(bigDecimal);
    String format = fastPrintf.format(args);
    System.out.println(format);
  }

  @Test
  public void testJoin() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    Args args =
        Args.of(
            random.nextInt(),
            random.nextLong(),
            random.nextFloat(),
            random.nextDouble(),
            random.nextBoolean(),
            new String(bytes, StandardCharsets.UTF_8));
    FastPrintf fastPrintf = FastPrintf.compile("%s, %s, %s, %s, %s, %s");
    String joinResult =
        args.asList().stream()
            .map(FormatTraits::value)
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
    assertEquals(joinResult, fastPrintf.format(args));
  }
}
