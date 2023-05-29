package org.fastprintf.traits;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntTraitsTest {

  @Test
  public void test() {
    IntTraits intTraits = new IntTraits(-123456789);
    assertEquals("-123456789", intTraits.asString());
    assertEquals(-123456789, intTraits.asInt());
    assertEquals(4171510507L, intTraits.asUnsignedLong());
    assertEquals(-123456789D, intTraits.asDouble(), 0.0000001);

    assertTrue(intTraits.isNegative());
    FormatContext context = new FormatContext(EnumSet.of(Flag.LEFT_JUSTIFY), false, false);
    assertEquals("-123456789", intTraits.forSignedDecimalInteger(context).toString());
    assertEquals("4171510507", intTraits.forUnsignedDecimalInteger(context).toString());
    assertEquals("F8A432EB", intTraits.forUnsignedHexadecimalInteger(context, true).toString());
    assertEquals("37051031353", intTraits.forUnsignedOctalInteger(context).toString());
    assertEquals("-123456789.000000", intTraits.forDecimalFloatingPoint(context, false).toString());
    assertEquals("-1.234568E+08", intTraits.forScientificNotation(context, true).toString());
    assertEquals("-1.23457e+08", intTraits.forUseShortestPresentation(context, false).toString());

    assertEquals("-1.d6f3454p26", intTraits.forHexadecimalFloatingPoint(context, false).toString());
  }
}
