package org.fastprintf;

import org.fastprintf.appender.Appender;
import org.fastprintf.appender.DefaultAppender;
import org.fastprintf.appender.FixedStringAppender;
import org.junit.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompilerTest {

  private void assertFixed(Appender appender, String value) {
    assertTrue(appender instanceof FixedStringAppender);
    assertEquals(value, ((FixedStringAppender) appender).getValue());
  }

  private void assertPattern(
      Appender appender, Specifier specifier, EnumSet<Flag> flags, int width, int precision) {
    assertTrue(appender instanceof DefaultAppender);
    DefaultAppender defaultAppender = (DefaultAppender) appender;
    assertEquals(specifier, defaultAppender.getSpecifier());
    FormatContext context = defaultAppender.getContext();
    assertEquals(flags, context.getFlags());
    assertEquals(width, context.getWidth());
    assertEquals(precision, context.getPrecision());
  }

  @Test
  public void test1() {
    Compiler compiler = new Compiler("Characters: %c %c \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(5, appenders.size());
    assertFixed(appenders.get(0), "Characters: ");
    assertPattern(appenders.get(1), Specifier.CHARACTER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(2), " ");
    assertPattern(appenders.get(3), Specifier.CHARACTER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(4), " \n");
  }

  @Test
  public void test2() {
    Compiler compiler = new Compiler("Decimals: %d %d\n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(5, appenders.size());
    assertFixed(appenders.get(0), "Decimals: ");
    assertPattern(
        appenders.get(1), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(2), " ");
    assertPattern(
        appenders.get(3), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(4), "\n");
  }

  @Test
  public void test3() {
    Compiler compiler = new Compiler("Preceding with zeros: %010d \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(3, appenders.size());
    assertFixed(appenders.get(0), "Preceding with zeros: ");
    assertPattern(
        appenders.get(1), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.of(Flag.ZERO_PAD), 10, -1);
    assertFixed(appenders.get(2), " \n");
  }

  @Test
  public void test4() {
    Compiler compiler = new Compiler("Some different radices: %d %x %o %#x %#o \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(11, appenders.size());
    assertFixed(appenders.get(0), "Some different radices: ");
    assertPattern(
        appenders.get(1), Specifier.SIGNED_DECIMAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(2), " ");
    assertPattern(
        appenders.get(3),
        Specifier.UNSIGNED_HEXADECIMAL_INTEGER,
        EnumSet.noneOf(Flag.class),
        -1,
        -1);
    assertFixed(appenders.get(4), " ");
    assertPattern(
        appenders.get(5), Specifier.UNSIGNED_OCTAL_INTEGER, EnumSet.noneOf(Flag.class), -1, -1);
    assertFixed(appenders.get(6), " ");
    assertPattern(
        appenders.get(7),
        Specifier.UNSIGNED_HEXADECIMAL_INTEGER,
        EnumSet.of(Flag.ALTERNATE),
        -1,
        -1);
    assertFixed(appenders.get(8), " ");
    assertPattern(
        appenders.get(9), Specifier.UNSIGNED_OCTAL_INTEGER, EnumSet.of(Flag.ALTERNATE), -1, -1);
    assertFixed(appenders.get(10), " \n");
  }

  @Test
  public void test5() {
    Compiler compiler = new Compiler("floats: %4.2f %+.0e %E \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(7, appenders.size());
    assertFixed(appenders.get(0), "floats: ");
    assertPattern(
        appenders.get(1), Specifier.DECIMAL_FLOATING_POINT, EnumSet.noneOf(Flag.class), 4, 2);
    assertFixed(appenders.get(2), " ");
    assertPattern(appenders.get(3), Specifier.SCIENTIFIC_NOTATION, EnumSet.of(Flag.PLUS), -1, 0);
    assertFixed(appenders.get(4), " ");
    assertPattern(
        appenders.get(5),
        Specifier.SCIENTIFIC_NOTATION_UPPERCASE,
        EnumSet.noneOf(Flag.class),
        -1,
        -1);
    assertFixed(appenders.get(6), " \n");
  }

  @Test
  public void test6() {
    Compiler compiler = new Compiler("Width trick: %*d \n");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(3, appenders.size());
    assertFixed(appenders.get(0), "Width trick: ");
    assertPattern(
        appenders.get(1),
        Specifier.SIGNED_DECIMAL_INTEGER,
        EnumSet.noneOf(Flag.class),
        Integer.MIN_VALUE,
        -1);
    assertFixed(appenders.get(2), " \n");
  }

  @Test
  public void test7() {
    Compiler compiler = new Compiler("%%%%");
    compiler.compile();
    List<Appender> appenders = compiler.getAppenders();
    assertEquals(2, appenders.size());
    assertFixed(appenders.get(0), "%");
    assertFixed(appenders.get(1), "%");
  }
}
