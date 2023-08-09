package org.fastprintf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

// https://github.com/BartMassey/printf-tests/blob/master/sources/tests-npm-printf.coffee
public class NpmCasesTest {

  private static void assertFormatResult(String pattern, String expected, Object... value) {
    FastPrintf fastPrintf = FastPrintf.compile(pattern).enableThreadLocalCache();
    Args args = Args.of(value);
    String format = fastPrintf.format(args);
    assertEquals(pattern, expected, format);
  }

  private static void gcAndPause() {
    //    System.gc();
    //    try {
    //      Thread.sleep(1000);
    //    } catch (InterruptedException e) {
    //      throw new RuntimeException(e);
    //    }
  }

  @Test
  public void test() {
    //    printf('% d', 42).should.eql    ' 42'
    //    printf('% d', -42).should.eql   '-42'
    //    printf('% 5d', 42).should.eql   '   42'
    //    printf('% 5d', -42).should.eql  '  -42'
    //    printf('% 15d', 42).should.eql  '             42'
    //    printf('% 15d', -42).should.eql '            -42'
    assertFormatResult("% d", " 42", 42);
    assertFormatResult("%+d", "+42", 42);
    assertFormatResult("% 5d", "   42", 42);
    assertFormatResult("% 5d", "  -42", -42);
    assertFormatResult("% 15d", "             42", 42);
    assertFormatResult("% 15d", "            -42", -42);

    gcAndPause();

    //    printf('%+d', 42).should.eql    '+42'
    //    printf('%+d', -42).should.eql   '-42'
    //    printf('%+5d', 42).should.eql   '  +42'
    //    printf('%+5d', -42).should.eql  '  -42'
    //    printf('%+15d', 42).should.eql  '            +42'
    //    printf('%+15d', -42).should.eql '            -42'
    assertFormatResult("%+d", "+42", 42);
    assertFormatResult("%+d", "-42", -42);
    assertFormatResult("%+5d", "  +42", 42);
    assertFormatResult("%+5d", "  -42", -42);
    assertFormatResult("%+15d", "            +42", 42);
    assertFormatResult("%+15d", "            -42", -42);

    gcAndPause();

    //    printf('%0d', 42).should.eql    '42'
    //    printf('%0d', -42).should.eql   '-42'
    //    printf('%05d', 42).should.eql   '00042'
    //    printf('%05d', -42).should.eql  '-00042'
    //    printf('%015d', 42).should.eql  '000000000000042'
    //    printf('%015d', -42).should.eql '-000000000000042'
    assertFormatResult("%0d", "42", 42);
    assertFormatResult("%0d", "-42", -42);
    assertFormatResult("%05d", "00042", 42);
    assertFormatResult("%05d", "-0042", -42);
    assertFormatResult("%015d", "000000000000042", 42);
    assertFormatResult("%015d", "-00000000000042", -42);

    gcAndPause();

    //    printf('%-d', 42).should.eql     '42'
    //    printf('%-d', -42).should.eql    '-42'
    //    printf('%-5d', 42).should.eql    '42   '
    //    printf('%-5d', -42).should.eql   '-42  '
    //    printf('%-15d', 42).should.eql   '42             '
    //    printf('%-15d', -42).should.eql  '-42            '
    //    printf('%-0d', 42).should.eql    '42'
    //    printf('%-0d', -42).should.eql   '-42'
    //    printf('%-05d', 42).should.eql   '42   '
    //    printf('%-05d', -42).should.eql  '-42  '
    //    printf('%-015d', 42).should.eql  '42             '
    //    printf('%-015d', -42).should.eql '-42            '
    //    printf('%0-d', 42).should.eql    '42'
    //    printf('%0-d', -42).should.eql   '-42'
    //    printf('%0-5d', 42).should.eql   '42   '
    //    printf('%0-5d', -42).should.eql  '-42  '
    //    printf('%0-15d', 42).should.eql  '42             '
    //    printf('%0-15d', -42).should.eql '-42
    assertFormatResult("%-d", "42", 42);
    assertFormatResult("%-d", "-42", -42);
    assertFormatResult("%-5d", "42   ", 42);
    assertFormatResult("%-5d", "-42  ", -42);
    assertFormatResult("%-15d", "42             ", 42);
    assertFormatResult("%-15d", "-42            ", -42);
    assertFormatResult("%-0d", "42", 42);
    assertFormatResult("%-0d", "-42", -42);
    assertFormatResult("%-05d", "42   ", 42);
    assertFormatResult("%-05d", "-42  ", -42);
    assertFormatResult("%-015d", "42             ", 42);
    assertFormatResult("%-015d", "-42            ", -42);
    assertFormatResult("%0-d", "42", 42);
    assertFormatResult("%0-d", "-42", -42);
    assertFormatResult("%0-5d", "42   ", 42);
    assertFormatResult("%0-5d", "-42  ", -42);
    assertFormatResult("%0-15d", "42             ", 42);
    assertFormatResult("%0-15d", "-42            ", -42);

    gcAndPause();

    //    printf('%d', 42.8952).should.eql     '42'
    //    printf('%.2d', 42.8952).should.eql   '42' # Note: the %d format is an int
    //    printf('%.2i', 42.8952).should.eql   '42'
    //    printf('%.2f', 42.8952).should.eql   '42.90'
    //    printf('%.2F', 42.8952).should.eql   '42.90'
    //    printf('%.10f', 42.8952).should.eql  '42.8952000000'
    //    printf('%1.2f', 42.8952).should.eql  '42.90'
    //    printf('%6.2f', 42.8952).should.eql  ' 42.90'
    //    printf('%06.2f', 42.8952).should.eql '042.90'
    //    printf('%+6.2f', 42.8952).should.eql '+42.90'
    //    printf('%5.10f', 42.8952).should.eql '42.8952000000'
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

    gcAndPause();

    //    printf('%*s', 'foo', 4).should.eql ' foo'
    //    printf('%*.*f', 3.14159265, 10, 2).should.eql '      3.14'
    //    printf('%0*.*f', 3.14159265, 10, 2).should.eql '0000003.14'
    //    printf('%-*.*f', 3.14159265, 10, 2).should.eql '3.14      '
    assertFormatResult("%*s", " foo", 4, "foo");
    assertFormatResult("%*.*f", "      3.14", 10, 2, 3.14159265);
    assertFormatResult("%0*.*f", "0000003.14", 10, 2, 3.14159265);
    assertFormatResult("%-*.*f", "3.14      ", 10, 2, 3.14159265);

    gcAndPause();

    //    printf('+%s+', 'hello').should.eql '+hello+'
    //    printf('+%d+', 10).should.eql '+10+'
    //    printf('%c', 'a').should.eql 'a'
    //    printf('%c', 34).should.eql '\"'
    //    printf('%c', 36).should.eql '$'
    //    printf('%d', 10).should.eql '10'
    assertFormatResult("+%s+", "+hello+", "hello");
    assertFormatResult("+%d+", "+10+", 10);
    assertFormatResult("%c", "a", 'a');
    assertFormatResult("%c", "\"", 34);
    assertFormatResult("%c", "$", 36);
    assertFormatResult("%d", "10", 10);
  }
}
