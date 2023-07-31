package org.fastprintf.box;

import org.fastprintf.seq.Seq;
import org.fastprintf.util.DoubleConsts;
import org.fastprintf.util.FormattedFloatingDecimal;

public final class DoubleBox implements FloatFamily {

  private static final double SCALE_UP = Math.scalb(1.0, 54);

  private final double value;

  public DoubleBox(double value) {
    this.value = value;
  }

  private static Seq seqOrNull(char[] ch) {
    return ch != null ? Seq.forArray(ch) : null;
  }

  private static FloatLayout simpleHexLayout(double d) {
    String s = Double.toHexString(d);
    int p = s.indexOf('p');
    assert (p >= 0);
    return new FloatLayout(Seq.wrap(s.substring(2, p)), Seq.wrap(s.substring(p + 1)), 0);
  }

  @Override
  public int signum() {
    return Double.compare(value, 0D);
  }

  @Override
  public boolean isNaN() {
    return Double.isNaN(value);
  }

  @Override
  public boolean isInfinite() {
    return Double.isInfinite(value);
  }

  private FloatLayout toLayout(int precision, FormattedFloatingDecimal.Form form) {
    FormattedFloatingDecimal fd =
        FormattedFloatingDecimal.valueOf(Math.abs(value), precision, form);
    return new FloatLayout(
        seqOrNull(fd.getMantissa()), seqOrNull(fd.getExponent()), fd.getExponentRounded());
  }

  @Override
  public FloatLayout generalLayout(int precision) {
    if (value == 0D) {
      return new FloatLayout(Seq.singleChar('0'), null, 0);
    }
    return toLayout(precision, FormattedFloatingDecimal.Form.GENERAL);
  }

  @Override
  public FloatLayout scientificLayout(int precision) {
    if (value == 0D) {
      return new FloatLayout(Seq.singleChar('0'), Seq.wrap("+00"), 0);
    }
    return toLayout(precision, FormattedFloatingDecimal.Form.SCIENTIFIC);
  }

  @Override
  public FloatLayout decimalLayout(int precision) {
    return toLayout(precision, FormattedFloatingDecimal.Form.DECIMAL_FLOAT);
  }

  @Override
  public FloatLayout hexLayout(int prec) {
    double d = Math.abs(value);
    // Let Double.toHexString handle simple cases
    if (!Double.isFinite(d) || d == 0.0 || prec == 0 || prec >= 13) {
      return simpleHexLayout(d);
    }

    assert (prec >= 1 && prec <= 12);

    int exponent = Math.getExponent(d);
    boolean subnormal = (exponent == Double.MIN_EXPONENT - 1);

    // If this is subnormal input so normalize (could be faster to
    // do as integer operation).
    if (subnormal) {
      d *= SCALE_UP;
      // Calculate the exponent.  This is not just exponent + 54
      // since the former is not the normalized exponent.
      exponent = Math.getExponent(d);
      assert exponent >= Double.MIN_EXPONENT && exponent <= Double.MAX_EXPONENT : exponent;
    }

    int precision = 1 + prec * 4;
    int shiftDistance = DoubleConsts.SIGNIFICAND_WIDTH - precision;
    assert (shiftDistance >= 1 && shiftDistance < DoubleConsts.SIGNIFICAND_WIDTH);

    long doppel = Double.doubleToLongBits(d);
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
      return new FloatLayout(Seq.wrap("1.0"), Seq.wrap("1024"), 0);
    } else {
      if (!subnormal) return simpleHexLayout(result);
      else {
        String s = Double.toHexString(result);
        // Create a normalized subnormal string.
        int idx = s.indexOf('p');
        if (idx >= 0) {
          // Get exponent and append at the end.
          String exp = s.substring(idx + 1);
          int iexp = Integer.parseInt(exp) - 54;
          return new FloatLayout(
              Seq.wrap(s.substring(2, idx)), Seq.wrap(Integer.toString(iexp)), iexp);
        } else {
          throw new AssertionError("Invalid hex string: " + s);
        }
      }
    }
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }
}
