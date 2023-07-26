package org.fastprintf.traits;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.seq.Seq;
import sun.misc.DoubleConsts;
import sun.misc.FormattedFloatingDecimal;

@SuppressWarnings("DuplicatedCode")
abstract class AbstractNumericTraits {

  private static final double SCALE_UP = Math.scalb(1.0, 54);

  static Seq nan(boolean upperCase) {
    return Seq.wrap(upperCase ? "NAN" : "NaN");
  }

  static Seq infinity(boolean upperCase, boolean negative, FormatContext context) {
    Seq seq = Seq.wrap(upperCase ? "INFINITY" : "Infinity");
    return handleSign(seq, negative, context);
  }

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

  static Seq handleZeroPadding(Seq seq, int reservedLen, FormatContext context) {
    int len = seq.length() + reservedLen;
    if (context.hasFlag(Flag.ZERO_PAD) && !context.hasFlag(Flag.LEFT_JUSTIFY)) {
      int width = context.getWidth();
      if (width > len) {
        return seq.prepend(Seq.repeated('0', width - len));
      }
    }
    return seq;
  }

  static Seq handleSignAndZeroPadding(Seq seq, boolean isNegative, FormatContext context) {
    return handleSign(handleZeroPadding(seq, isNegative ? 1 : 0, context), isNegative, context);
  }


  double asDouble(){
    return 0.0;
  }

  boolean isNegative(){
    return false;
  }

  Seq forDecimalFloatingPoint(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return nan(upper);
    if (Double.isInfinite(value0)) return infinity(upper, isNegative(), context);
    double value = Math.abs(value0);
    int precision = context.getPrecision();
    int prec = (precision == -1 ? 6 : precision);

    FormattedFloatingDecimal fd =
        FormattedFloatingDecimal.valueOf(value, prec, FormattedFloatingDecimal.Form.DECIMAL_FLOAT);

    Seq mant = addZeros(Seq.forArray(fd.getMantissa()), prec);
    // If the precision is zero and the '#' flag is set, add the
    // requested decimal point.
    if (context.hasFlag(Flag.ALTERNATE) && (prec == 0)) mant = addDot(mant);
    return handleSignAndZeroPadding(upper ? Seq.upperCase(mant) : mant, isNegative(), context);
  }

  Seq forScientificNotation(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return nan(upper);
    if (Double.isInfinite(value0)) return infinity(upper, isNegative(), context);
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

    return handleSignAndZeroPadding(
        buildScientific(mant, Seq.forArray(exp), upper), isNegative(), context);
  }

  Seq forUseShortestPresentation(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return nan(upper);
    if (Double.isInfinite(value0)) return infinity(upper, isNegative(), context);
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
      return handleSignAndZeroPadding(
          buildScientific(mantSeq, Seq.forArray(exp), upper), isNegative(), context);
    } else {
      return handleSignAndZeroPadding(mantSeq, isNegative(), context);
    }
  }

  // TODO

  Seq forHexadecimalFloatingPoint(FormatContext context, boolean upper) {
    double value0 = asDouble();
    if (Double.isNaN(value0)) return nan(upper);
    if (Double.isInfinite(value0)) return infinity(upper, isNegative(), context);
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
      return handleSignAndZeroPadding(
          Seq.wrap(Double.toHexString(value).substring(2)), isNegative(), context);
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
            throw new AssertionError();
          } else {
            // Get exponent and append at the end.
            String exp = res.substring(idx + 1);
            int iexp = Integer.parseInt(exp) - 54;
            char p = upper ? 'P' : 'p';
            return handleSignAndZeroPadding(
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



}
