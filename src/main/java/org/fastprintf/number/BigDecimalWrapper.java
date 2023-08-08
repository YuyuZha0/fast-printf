package org.fastprintf.number;

import org.fastprintf.seq.Seq;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class BigDecimalWrapper implements FloatForm {

  private final BigDecimal value;
  private final int signum;

  public BigDecimalWrapper(BigDecimal value) {
    this.value = value.abs();
    this.signum = value.signum();
  }

  public BigDecimalWrapper(BigInteger unscaledValue, int scale) {
    this.value = new BigDecimal(unscaledValue.abs(), scale);
    this.signum = unscaledValue.signum();
  }

  private static FloatLayout scientificLayout0(BigDecimal value) {
    BigInteger unscaledValue = value.unscaledValue();
    int scale = value.scale();
    String unscaledString = unscaledValue.toString();
    int length = unscaledString.length();
    if (scale == 0) {
      if (length > 1) {
        Seq mant = Seq.ch(unscaledString.charAt(0));
        mant = mant.append(Seq.ch('.'));
        mant = mant.append(Seq.wrap(unscaledString.substring(1, length - 1)));
        Seq exp = length < 10 ? Seq.wrap("+0" + (length - 1)) : Seq.wrap("+" + (length - 1));
        return new FloatLayout(mant, exp);
      } else {
        return new FloatLayout(Seq.wrap(unscaledString), Seq.wrap("+00"));
      }
    }
    Seq mant;
    if (length > 1) {
      mant = Seq.ch(unscaledString.charAt(0));
      mant = mant.append(Seq.ch('.'));
      mant = mant.append(Seq.wrap(unscaledString.substring(1, length - 1)));
    } else {
      mant = Seq.wrap(unscaledString);
    }
    long adjusted = -(long) scale + (length - 1);
    Seq exp;
    if (adjusted != 0) {
      long abs = Math.abs(adjusted);
      exp = Seq.ch(adjusted < 0 ? '-' : '+');
      if (abs < 10) {
        exp = exp.append(Seq.ch('0'));
      }
      exp = exp.append(Seq.wrap(Long.toString(abs)));
    } else {
      exp = Seq.wrap("+00");
    }

    return new FloatLayout(mant, exp);
  }

  private static FloatLayout decimalLayout0(BigDecimal value) {
    BigInteger unscaledValue = value.unscaledValue();
    int scale = value.scale();
    String unscaledString = unscaledValue.toString();
    if (scale == 0) {
      return new FloatLayout(Seq.wrap(unscaledString), null);
    }
    int length = unscaledString.length();
    int pad = scale - length;
    if (pad >= 0) {
      Seq mant = Seq.wrap(unscaledString);
      if (pad > 0) {
        mant = mant.prepend(Seq.repeated('0', pad));
      }
      mant = mant.prepend(Seq.wrap("0."));
      return new FloatLayout(mant, null);
    }
    if (-pad < length) {
      Seq mant = Seq.wrap(unscaledString.substring(0, -pad));
      mant = mant.append(Seq.ch('.'));
      mant = mant.append(Seq.wrap(unscaledString.substring(-pad, scale)));
      return new FloatLayout(mant, null);
    }

    Seq mant = Seq.wrap(unscaledString);
    mant = mant.append(Seq.repeated('0', -scale));
    return new FloatLayout(mant, null);
  }

  @Override
  public int signum() {
    return signum;
  }

  @Override
  public boolean isNaN() {
    return false;
  }

  @Override
  public boolean isInfinite() {
    return false;
  }

  @Override
  public FloatLayout generalLayout(int precision) {
    BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);
    BigDecimal tenToThePrec = BigDecimal.valueOf(1, -precision);
    if ((value.equals(BigDecimal.ZERO))
        || ((value.compareTo(tenToTheNegFour) >= 0) && (value.compareTo(tenToThePrec) < 0))) {

      int e = -value.scale() + (value.unscaledValue().toString().length() - 1);

      // xxx.yyy
      //   g precision (# sig digits) = #x + #y
      //   f precision = #y
      //   exponent = #x - 1
      // => f precision = g precision - exponent - 1
      // 0.000zzz
      //   g precision (# sig digits) = #z
      //   f precision = #0 (after '.') + #z
      //   exponent = - #0 (after '.') - 1
      // => f precision = g precision - exponent - 1
      precision = precision - e - 1;
      return decimalLayout(precision);
    } else {
      return scientificLayout(precision - 1);
    }
  }

  @Override
  public FloatLayout scientificLayout(int precision) {
    int scale = value.scale();
    int origPrec = value.precision();
    int compPrec;

    if (precision > origPrec - 1) {
      compPrec = origPrec;
    } else {
      compPrec = precision + 1;
    }
    MathContext mc = new MathContext(compPrec);
    BigDecimal v = new BigDecimal(value.unscaledValue(), scale, mc);
    return scientificLayout0(v);
  }

  @Override
  public FloatLayout decimalLayout(int precision) {
    BigDecimal value = this.value;
    int scale = value.scale();
    if (scale > precision) {
      // more "scale" digits than the requested "precision"
      int compPrec = value.precision();
      if (compPrec <= scale) {
        // case of 0.xxxxxx
        value = value.setScale(precision, RoundingMode.HALF_UP);
      } else {
        compPrec -= (scale - precision);
        value = new BigDecimal(value.unscaledValue(), scale, new MathContext(compPrec));
      }
    }
    return decimalLayout0(value);
  }

  @Override
  public FloatLayout hexLayout(int precision) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    if (signum >= 0) {
      return value.toString();
    } else {
      return "-" + value.toString();
    }
  }
}
