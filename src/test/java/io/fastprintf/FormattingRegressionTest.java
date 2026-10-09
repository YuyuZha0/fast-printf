package io.fastprintf;

import static org.junit.Assert.*;

import io.fastprintf.seq.Seq;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import org.junit.Test;

public class FormattingRegressionTest {
  @Test
  public void alternateOctalCountsLeadingZeroAsPrecision() {
    assertEquals("0", FastPrintf.compile("%#.0o").format(0));
    assertEquals("000", FastPrintf.compile("%#.3o").format(0));
    assertEquals("0000", FastPrintf.compile("%#04o").format(0));
    assertEquals("012", FastPrintf.compile("%#.3o").format(10));
    assertEquals("0012", FastPrintf.compile("%#.4o").format(10));
    assertEquals("0000", FastPrintf.compile("%#04x").format(0));
    assertEquals("", FastPrintf.compile("%#.0x").format(0));
  }

  @Test
  public void suppressedIntegerDigitsStillKeepSign() {
    assertEquals(" ", FastPrintf.compile("% .0d").format(0));
    assertEquals("+", FastPrintf.compile("%+ .0d").format(0));
  }

  @Test
  public void alternateGeneralPadsScientificMantissa() {
    assertEquals("1.00000e+08", FastPrintf.compile("%#g").format(1e8));
    assertEquals("1.e+08", FastPrintf.compile("%#.1g").format(1e8));
    assertEquals("1.00000E-08", FastPrintf.compile("%#G").format(new BigDecimal("1e-8")));
  }

  @Test
  public void bigDecimalGeneralChoosesNotationAfterRounding() {
    assertEquals("1e+03", FastPrintf.compile("%.3g").format(new BigDecimal("999.9")));
    assertEquals("0.0001", FastPrintf.compile("%.3g").format(new BigDecimal("0.00009999")));
    assertEquals("0", FastPrintf.compile("%g").format(new BigDecimal("0.000")));
    assertEquals("0.00000", FastPrintf.compile("%#g").format(new BigDecimal("0.000")));
    assertEquals("0.000e+00", FastPrintf.compile("%.3e").format(new BigDecimal("0.000")));
  }

  @Test
  public void compositeSlicesKeepTheirOriginalLength() {
    Seq joined = Seq.join(Arrays.asList(Seq.wrap("12"), Seq.wrap("34"), Seq.wrap("56")));
    Seq slice = joined.subSequence(1, 5);
    assertEquals("2345", slice.toString());
    assertEquals(4, slice.length());
    assertEquals("34", slice.subSequence(1, 3).toString());
    assertEquals("2345!", slice.append(Seq.ch('!')).toString());
    assertEquals(5, slice.append(Seq.ch('!')).length());
  }

  @Test
  public void minimumDynamicWidthIsRejected() {
    assertThrows(
        PrintfException.class, () -> FastPrintf.compile("%*d").format(Integer.MIN_VALUE, 1));
  }

  @Test
  public void decimalDigitsMatchJdkAcrossTypesAndDestinations() {
    Number[] values = {
      Byte.MIN_VALUE,
      (byte) 0,
      Byte.MAX_VALUE,
      Short.MIN_VALUE,
      (short) 0,
      Short.MAX_VALUE,
      Integer.MIN_VALUE,
      0,
      Integer.MAX_VALUE,
      Long.MIN_VALUE,
      0L,
      Long.MAX_VALUE,
      new BigInteger("-123456789012345678901234567890")
    };
    for (String pattern : new String[] {"%d", "%+30d", "%030d", "%-30d", "% 30d"}) {
      FastPrintf formatter = FastPrintf.compile(pattern);
      for (Number value : values) {
        String expected = String.format(Locale.ROOT, pattern, value);
        assertEquals(pattern + ": " + value, expected, formatter.format(value));
        StringWriter writer = new StringWriter();
        formatter.format(writer, Args.of(value));
        assertEquals(expected, writer.toString());
      }
    }
  }

  @Test
  public void numericFlagsAgreeWithJdkForSharedSemantics() {
    Random random = new Random(42);
    for (String pattern : new String[] {"%d", "%+020d", "%-20d", "%#020x", "%#020X", "%#020o"}) {
      FastPrintf formatter = FastPrintf.compile(pattern);
      for (int i = 0; i < 100; i++) {
        long value = random.nextLong();
        assertEquals(pattern, String.format(Locale.ROOT, pattern, value), formatter.format(value));
      }
    }
    for (String pattern :
        new String[] {"%.3f", "%+020.3f", "%-20.3f", "%#.0f", "%#.0e", "%18.4E"}) {
      FastPrintf formatter = FastPrintf.compile(pattern);
      for (double value :
          new double[] {
            0.0,
            -0.0,
            1.25,
            -1.25,
            999.9999,
            1e-8,
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
          }) {
        assertEquals(pattern, String.format(Locale.ROOT, pattern, value), formatter.format(value));
      }
    }
  }

  @Test
  public void dynamicFieldsMatchTheirStaticEquivalents() {
    for (int width : new int[] {-20, 0, 20}) {
      for (int precision : new int[] {-1, Integer.MIN_VALUE, 0, 3}) {
        String pattern =
            "%"
                + (width < 0 ? "-" : "")
                + (width == 0 ? "" : Math.abs(width))
                + (precision < 0 ? "" : "." + precision)
                + "f";
        assertEquals(
            FastPrintf.compile(pattern).format(-12.25),
            FastPrintf.compile("%*.*f").format(width, precision, -12.25));
      }
    }
    assertThrows(PrintfException.class, () -> FastPrintf.compile("%*f").format(65537, 1.0));
    assertThrows(PrintfException.class, () -> FastPrintf.compile("%*f").format(-65537, 1.0));
    assertThrows(PrintfException.class, () -> FastPrintf.compile("%.*f").format(65537, 1.0));
  }

  @Test
  public void changedContextCannotMutateItsSourceFlags() {
    FormatContext original = FormatContext.create("+");
    original.setWidth(20).addFlag(Flag.LEFT_JUSTIFY);
    original.setPrecision(3).addFlag(Flag.ZERO_PAD);
    assertFalse(original.hasFlag(Flag.LEFT_JUSTIFY));
    assertFalse(original.hasFlag(Flag.ZERO_PAD));
  }

  @Test
  public void nonFiniteFloatsRespectSpaceAndUppercase() {
    for (char specifier : new char[] {'f', 'e', 'g', 'a', 'F', 'E', 'G', 'A'}) {
      String inf = Character.isUpperCase(specifier) ? "INFINITY" : "Infinity";
      assertEquals(
          " " + inf, FastPrintf.compile("% " + specifier).format(Double.POSITIVE_INFINITY));
      assertEquals(
          "-" + inf, FastPrintf.compile("% " + specifier).format(Double.NEGATIVE_INFINITY));
    }
  }

  @Test
  public void cachedFormatterSupportsReentrantObjectFormatting() {
    FastPrintf formatter = FastPrintf.compile("[%s]").enableThreadLocalCache();
    Object recursive =
        new Object() {
          @Override
          public String toString() {
            return formatter.format("inner");
          }
        };
    assertEquals("[[inner]]", formatter.format(recursive));
    assertEquals("[next]", formatter.format("next"));
  }

  @Test
  public void cachedFormatterRecoversAfterFailuresAndOversizedResults() {
    FastPrintf formatter = FastPrintf.compile("[%s]").enableThreadLocalCache();
    assertThrows(PrintfException.class, () -> formatter.format(Args.create()));
    assertEquals("[ok]", formatter.format("ok"));
    char[] chars = new char[70000];
    Arrays.fill(chars, 'x');
    String large = new String(chars);
    assertEquals("[" + large + "]", formatter.format(large));
    assertEquals("[small]", formatter.format("small"));
    FastPrintf largeCapacity = formatter.setStringBuilderInitialCapacity(70000);
    assertEquals("[a]", largeCapacity.format("a"));
    assertEquals("[b]", largeCapacity.format("b"));
  }

  @Test
  public void everyCompositeSliceMatchesString() {
    Seq rope = Seq.wrap("ab").append(Seq.wrap("cde")).append(Seq.wrap("fgh"));
    String value = rope.toString();
    for (int start = 0; start <= value.length(); start++) {
      for (int end = start; end <= value.length(); end++) {
        Seq slice = rope.subSequence(start, end);
        assertEquals(end - start, slice.length());
        assertEquals(value.substring(start, end), slice.toString());
      }
    }
  }
}
