package org.fastprintf.traits;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;
import sun.misc.DoubleConsts;
import sun.misc.FormattedFloatingDecimal;

@SuppressWarnings("DuplicatedCode")
abstract class AbstractTraits implements FormatTraits {

  private static final double SCALE_UP = Math.scalb(1.0, 54);

  static Seq addZeros(Seq v, int prec) {
    // Look for the dot.  If we don't find one, the we'll need to add
    // it before we add the zeros.
    int length = v.length();
    int i;
    for (i = 0; i < length; i++) {
      if (v.charAt(i) == '.') break;
    }
    boolean needDot = (i == length);

    // Determine existing precision.
    int outPrec = length - i - (needDot ? 0 : 1);
    assert (outPrec <= prec);
    if (outPrec == prec) return v;

    // Add dot if previously determined to be necessary.
    if (needDot) {
      v = v.append(Seq.singleChar('.'));
    }
    return v.append(Seq.repeated('0', prec - outPrec));
  }

  // Add a '.' to th mantissa if required
  static Seq addDot(Seq mant) {
    return mant.append(Seq.singleChar('.'));
  }

  static Seq buildScientific(Seq mant, Seq exp, boolean upper) {
    return Seq.concat(mant, Seq.singleChar(upper ? 'E' : 'e'), exp);
  }

  static Seq handleDigitNumber(Seq s, int precision) {
    int length = s.length();
    if (precision <= length) {
      return s;
    }
    return s.prepend(Seq.repeated('0', precision - length));
  }

  static Seq handleSign(Seq seq, boolean isNegative, FormatContext context) {
    if (isNegative) {
      return seq.prepend(Seq.singleChar('-'));
    } else if (context.hasFlag(Flag.PLUS)) {
      return seq.prepend(Seq.singleChar('+'));
    } else if (context.hasFlag(Flag.LEADING_SPACE)) {
      return seq.prepend(Seq.singleChar(' '));
    } else {
      return seq;
    }
  }

  @Override
  public final boolean isNull() {
    return false;
  }

  @Override
  public final Seq seqForSpecifier(Specifier specifier, FormatContext context) {
    switch (specifier) {
      case SIGNED_DECIMAL_INTEGER:
        return forSignedDecimalInteger(context);
      case UNSIGNED_DECIMAL_INTEGER:
        return forUnsignedDecimalInteger(context);
      case UNSIGNED_HEXADECIMAL_INTEGER:
        return forUnsignedHexadecimalInteger(context, false);
      case UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE:
        return forUnsignedHexadecimalInteger(context, true);
      case UNSIGNED_OCTAL_INTEGER:
        return forUnsignedOctalInteger(context);
      case DECIMAL_FLOATING_POINT:
        return forDecimalFloatingPoint(context, false);
      case DECIMAL_FLOATING_POINT_UPPERCASE:
        return forDecimalFloatingPoint(context, true);
      case SCIENTIFIC_NOTATION:
        return forScientificNotation(context, false);
      case SCIENTIFIC_NOTATION_UPPERCASE:
        return forScientificNotation(context, true);
      case USE_SHORTEST_PRESENTATION:
        return forUseShortestPresentation(context, false);
      case USE_SHORTEST_PRESENTATION_UPPERCASE:
        return forUseShortestPresentation(context, true);
      case HEXADECIMAL_FLOATING_POINT:
        return forHexadecimalFloatingPoint(context, false);
      case HEXADECIMAL_FLOATING_POINT_UPPERCASE:
        return forHexadecimalFloatingPoint(context, true);
      case STRING:
        return Seq.wrap(asString());
      case CHARACTER:
        return forCharacter();
      case PERCENT_SIGN:
        return Seq.singleChar('%');
      default:
        return Seq.empty();
    }
  }

  Seq forSignedDecimalInteger(FormatContext context) {
    long value = asLong();
    int precision = context.getPrecision();
    if (value == 0 && precision == 0) {
      return Seq.empty();
    }
    return handleSign(
        handleDigitNumber(Seq.wrap(Long.toString(Math.abs(value))), precision),
        isNegative(),
        context);
  }

  Seq forUnsignedDecimalInteger(FormatContext context) {
    long value = asUnsignedLong();
    int precision = context.getPrecision();
    if (value == 0 && precision == 0) {
      return Seq.empty();
    }
    return handleDigitNumber(Seq.wrap(Long.toUnsignedString(value)), precision);
  }

  Seq forUnsignedHexadecimalInteger(FormatContext context, boolean upper) {
    long value = asUnsignedLong();
    int precision = context.getPrecision();
    if (value == 0 && precision == 0) {
      return Seq.empty();
    }
    String s = Long.toHexString(value);
    Seq seq = upper ? Seq.upperCase(s) : Seq.wrap(s);
    seq = handleDigitNumber(seq, precision);
    if (context.hasFlag(Flag.ALTERNATE)) {
      return upper ? seq.prepend(Seq.wrap("0X")) : seq.prepend(Seq.wrap("0x"));
    }
    return seq;
  }

  Seq forUnsignedOctalInteger(FormatContext context) {
    long value = asUnsignedLong();
    int precision = context.getPrecision();
    if (value == 0 && precision == 0) {
      return Seq.empty();
    }
    Seq seq = Seq.wrap(Long.toOctalString(value));
    seq = handleDigitNumber(seq, precision);
    if (context.hasFlag(Flag.ALTERNATE)) {
      return seq.prepend(Seq.singleChar('0'));
    }
    return seq;
  }

  Seq forDecimalFloatingPoint(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return upper ? Seq.wrap("NAN") : Seq.wrap("NaN");
    if (Double.isInfinite(value0)) {
      Seq seq = upper ? Seq.wrap("INFINITY") : Seq.wrap("Infinity");
      return handleSign(seq, isNegative(), context);
    }

    double value = Math.abs(value0);
    int precision = context.getPrecision();
    int prec = (precision == -1 ? 6 : precision);

    FormattedFloatingDecimal fd =
        FormattedFloatingDecimal.valueOf(value, prec, FormattedFloatingDecimal.Form.DECIMAL_FLOAT);

    Seq mant = addZeros(Seq.forArray(fd.getMantissa()), prec);
    // If the precision is zero and the '#' flag is set, add the
    // requested decimal point.
    if (context.hasFlag(Flag.ALTERNATE) && (prec == 0)) mant = addDot(mant);
    return handleSign(upper ? Seq.upperCase(mant) : mant, isNegative(), context);
  }

  Seq forScientificNotation(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return upper ? Seq.wrap("NAN") : Seq.wrap("NaN");
    if (Double.isInfinite(value0)) {
      Seq seq = upper ? Seq.wrap("INFINITY") : Seq.wrap("Infinity");
      return handleSign(seq, isNegative(), context);
    }
    // Create a new FormattedFloatingDecimal with the desired
    // precision.
    int precision = context.getPrecision();
    int prec = (precision == -1 ? 6 : precision);
    double value = Math.abs(value0);

    FormattedFloatingDecimal fd =
        FormattedFloatingDecimal.valueOf(value, prec, FormattedFloatingDecimal.Form.SCIENTIFIC);

    Seq mant = addZeros(Seq.forArray(fd.getMantissa()), prec);

    // If the precision is zero and the '#' flag is set, add the
    // requested decimal point.
    if (context.hasFlag(Flag.ALTERNATE) && (prec == 0)) mant = addDot(mant);

    char[] exp = (value == 0.0) ? new char[] {'+', '0', '0'} : fd.getExponent();

    return handleSign(buildScientific(mant, Seq.forArray(exp), upper), isNegative(), context);
  }

  Seq forUseShortestPresentation(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return upper ? Seq.wrap("NAN") : Seq.wrap("NaN");
    if (Double.isInfinite(value0)) {
      Seq seq = upper ? Seq.wrap("INFINITY") : Seq.wrap("Infinity");
      return handleSign(seq, isNegative(), context);
    }
    int precision = context.getPrecision();
    int prec = precision;
    if (precision == -1) prec = 6;
    else if (precision == 0) prec = 1;
    double value = Math.abs(value0);

    char[] exp;
    char[] mant;
    int expRounded;
    if (value == 0.0) {
      exp = null;
      mant = new char[] {'0'};
      expRounded = 0;
    } else {
      FormattedFloatingDecimal fd =
          FormattedFloatingDecimal.valueOf(value, prec, FormattedFloatingDecimal.Form.GENERAL);
      exp = fd.getExponent();
      mant = fd.getMantissa();
      expRounded = fd.getExponentRounded();
    }

    if (exp != null) {
      prec -= 1;
    } else {
      prec -= expRounded + 1;
    }

    Seq mantSeq = addZeros(Seq.forArray(mant), prec);
    // If the precision is zero and the '#' flag is set, add the
    // requested decimal point.
    if (context.hasFlag(Flag.ALTERNATE) && (prec == 0)) mantSeq = addDot(mantSeq);
    if (exp != null) {
      return handleSign(buildScientific(mantSeq, Seq.forArray(exp), upper), isNegative(), context);
    } else {
      return handleSign(mantSeq, isNegative(), context);
    }
  }

  // TODO

  Seq forHexadecimalFloatingPoint(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return upper ? Seq.wrap("NAN") : Seq.wrap("NaN");
    if (Double.isInfinite(value0)) {
      Seq seq = upper ? Seq.wrap("INFINITY") : Seq.wrap("Infinity");
      return handleSign(seq, isNegative(), context);
    }
    int precision0 = context.getPrecision();
    int prec = precision0;
    if (precision0 == -1)
      // assume that we want all of the digits
      prec = 0;
    else if (precision0 == 0) prec = 1;
    double value = Math.abs(value0);

    // Let Double.toHexString handle simple cases
    if (!Double.isFinite(value) || value == 0.0 || prec == 0 || prec >= 13)
      // remove "0x"
      return handleSign(Seq.wrap(Double.toHexString(value).substring(2)), isNegative(), context);
    else {
      int exponent = Math.getExponent(value);
      boolean subnormal = (exponent == DoubleConsts.MIN_EXPONENT - 1);

      // If this is subnormal input so normalize (could be faster to
      // do as integer operation).
      if (subnormal) {
        value *= SCALE_UP;
        // Calculate the exponent.  This is not just exponent + 54
        // since the former is not the normalized exponent.
        exponent = Math.getExponent(value);
        assert exponent >= DoubleConsts.MIN_EXPONENT && exponent <= DoubleConsts.MAX_EXPONENT
            : exponent;
      }

      int precision = 1 + prec * 4;
      int shiftDistance = DoubleConsts.SIGNIFICAND_WIDTH - precision;
      assert (shiftDistance >= 1 && shiftDistance < DoubleConsts.SIGNIFICAND_WIDTH);

      long doppel = Double.doubleToLongBits(value);
      // Deterime the number of bits to keep.
      long newSignif =
          (doppel & (DoubleConsts.EXP_BIT_MASK | DoubleConsts.SIGNIF_BIT_MASK)) >> shiftDistance;
      // Bits to round away.
      long roundingBits = doppel & ~(~0L << shiftDistance);

      // To decide how to round, look at the low-order bit of the
      // working significand, the highest order discarded bit (the
      // round bit) and whether any of the lower order discarded bits
      // are nonzero (the sticky bit).

      boolean leastZero = (newSignif & 0x1L) == 0L;
      boolean round = ((1L << (shiftDistance - 1)) & roundingBits) != 0L;
      boolean sticky = shiftDistance > 1 && (~(1L << (shiftDistance - 1)) & roundingBits) != 0;
      if ((leastZero && round && sticky) || (!leastZero && round)) {
        newSignif++;
      }

      long signBit = doppel & DoubleConsts.SIGN_BIT_MASK;
      newSignif = signBit | (newSignif << shiftDistance);
      double result = Double.longBitsToDouble(newSignif);

      if (Double.isInfinite(result)) {
        // Infinite result generated by rounding
        return Seq.wrap(upper ? "1.0P1024" : "1.0p1024");
      } else {
        String res = Double.toHexString(result).substring(2);
        if (!subnormal) return handleSign(Seq.wrap(res), isNegative(), context);
        else {
          // Create a normalized subnormal string.
          int idx = res.indexOf('p');
          if (idx == -1) {
            // No 'p' character in hex string.
            assert false;
            return null;
          } else {
            // Get exponent and append at the end.
            String exp = res.substring(idx + 1);
            int iexp = Integer.parseInt(exp) - 54;
            char p = upper ? 'P' : 'p';
            return handleSign(
                Seq.wrap(res.substring(0, idx))
                    .append(Seq.singleChar(p))
                    .append(Seq.wrap(Integer.toString(iexp))),
                isNegative(),
                context);
          }
        }
      }
    }
  }

  Seq forCharacter() {
    int value = (int) asLong();
    if (value < 0) {
      throw new IllegalArgumentException("character value must be non-negative");
    }
    return Seq.singleChar((char) value);
  }
}
