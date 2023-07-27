package org.fastprintf.appender;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.FloatLayout;
import org.fastprintf.box.IntFamily;
import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

import java.util.function.Function;

public final class SeqFormatter {

  private static final Seq PLUS = Seq.singleChar('+');
  private static final Seq MINUS = Seq.singleChar('-');
  private static final Seq SPACE = Seq.singleChar(' ');
  private static final Seq DOT = Seq.singleChar('.');

  private static final Seq E = Seq.singleChar('e');

  private SeqFormatter() {
    throw new IllegalStateException();
  }

  private static int width(FormatContext context) {
    return context.isWidthSet() ? context.getWidth() : 0;
  }

  private static Seq spaceJustify(FormatContext context, Seq v0) {
    int width = width(context);
    int len = v0.length();
    if (width > len) {
      Seq pad = Seq.repeated(' ', width - len);
      if (context.hasFlag(Flag.LEFT_JUSTIFY)) {
        v0 = v0.append(pad);
      } else {
        v0 = v0.prepend(pad);
      }
    }
    return v0;
  }

  private static Seq sign(FormatContext context, Seq v0, boolean negative) {
    if (negative) {
      v0 = v0.prepend(MINUS);
    } else if (context.hasFlag(Flag.PLUS)) {
      v0 = v0.prepend(PLUS);
    } else if (context.hasFlag(Flag.LEADING_SPACE)) {
      v0 = v0.prepend(SPACE);
    }
    return v0;
  }

  private static Seq signAndJustify(FormatContext context, Seq v0, boolean negative) {
    if (!context.hasFlag(Flag.ZERO_PAD) || context.hasFlag(Flag.LEFT_JUSTIFY)) {
      return spaceJustify(context, sign(context, v0, negative));
    }
    int width = width(context);
    if (negative || context.hasFlag(Flag.PLUS) || context.hasFlag(Flag.LEADING_SPACE)) {
      --width;
    }
    int len = v0.length();
    if (width > len) {
      Seq pad = Seq.repeated('0', width - len);
      v0 = v0.prepend(pad);
    }
    return sign(context, v0, negative);
  }

  static Seq d(FormatContext context, IntFamily value) {
    int signum = value.signum();
    if (signum == 0 && context.getPrecision() == 0) {
      Seq v0 = context.hasFlag(Flag.PLUS) ? PLUS : Seq.empty();
      return spaceJustify(context, v0);
    }
    Seq v0 = Seq.wrap(value.toDecimalString());
    int precision = 1;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
    } else if (context.hasFlag(Flag.ZERO_PAD)
        && context.isWidthSet()
        && !context.hasFlag(Flag.LEFT_JUSTIFY)) {
      precision = context.getWidth();
      if (signum < 0 || context.hasFlag(Flag.PLUS) || context.hasFlag(Flag.LEADING_SPACE)) {
        --precision;
      }
    }
    if (precision > v0.length()) {
      Seq pad = Seq.repeated('0', precision - v0.length());
      v0 = v0.prepend(pad);
    }
    v0 = sign(context, v0, signum < 0);
    return spaceJustify(context, v0);
  }

  static Seq o(FormatContext context, IntFamily value) {
    return formatUnsignedInteger(context, value, IntFamily::toOctalString, "0");
  }

  static Seq x(FormatContext context, IntFamily value) {
    return formatUnsignedInteger(context, value, IntFamily::toHexString, "0x");
  }

  static Seq u(FormatContext context, IntFamily value) {
    return formatUnsignedInteger(context, value, IntFamily::toUnsignedDecimalString, "");
  }

  private static Seq formatUnsignedInteger(
      FormatContext context, IntFamily value, Function<IntFamily, String> toString, String prefix) {
    int signum = value.signum();
    if (signum == 0 && context.getPrecision() == 0) {
      return spaceJustify(context, Seq.empty());
    }
    Seq v0 = Seq.wrap(toString.apply(value));
    int precision = 1;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
    } else if (context.hasFlag(Flag.ZERO_PAD)
        && context.isWidthSet()
        && !context.hasFlag(Flag.LEFT_JUSTIFY)) {
      precision = context.getWidth();
    }
    if (context.hasFlag(Flag.ALTERNATE)) {
      precision -= prefix.length();
    }
    if (precision > v0.length()) {
      Seq pad = Seq.repeated('0', precision - v0.length());
      v0 = v0.prepend(pad);
    }
    if (signum != 0 && context.hasFlag(Flag.ALTERNATE)) {
      v0 = v0.prepend(Seq.wrap(prefix));
    }
    return spaceJustify(context, v0);
  }

  private static Seq nanOrInfinity(FormatContext context, FloatFamily value) {
    Seq v0;
    if (value.isNaN()) {
      v0 = Seq.wrap("NaN");
    } else {
      boolean neg = value.signum() < 0;
      if (neg) {
        v0 = Seq.wrap("-Infinity");
      } else if (context.hasFlag(Flag.PLUS)) {
        v0 = Seq.wrap("+Infinity");
      } else {
        v0 = Seq.wrap("Infinity");
      }
    }
    return spaceJustify(context, v0);
  }

  private static int indexOfDot(char[] chars) {
    int length = chars.length;
    for (int i = 0; i < length; i++) {
      if (chars[i] == '.') {
        return i;
      }
    }
    return -1;
  }

  static Seq f(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    int precision = 6;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
    }
    FloatLayout layout = value.decimalLayout(precision);
    Seq mantissa = addZeros(layout.getMantissa(), precision);
    if (precision == 0 && context.hasFlag(Flag.ALTERNATE)) {
      mantissa = mantissa.append(DOT);
    }
    return signAndJustify(context, mantissa, value.signum() < 0);
  }

  private static Seq addZeros(char[] mantissa, int precision) {
    int dot = indexOfDot(mantissa);
    Seq v0 = Seq.forArray(mantissa);
    int outPrecision = 0;
    if (dot >= 0) {
      outPrecision = mantissa.length - (dot + 1);
    }
    if (outPrecision == precision) {
      return v0;
    }
    if (dot < 0) {
      v0 = v0.append(DOT);
    }
    return v0.append(Seq.repeated('0', precision - outPrecision));
  }

  static Seq e(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    int precision = 6;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
    }
    FloatLayout layout = value.scientificLayout(precision);
    Seq v0 = addZeros(layout.getMantissa(), precision);
    if (precision == 0 && context.hasFlag(Flag.ALTERNATE)) {
      v0 = v0.append(DOT);
    }
    v0 = v0.append(E);
    v0 = v0.append(Seq.forArray(layout.getExponent()));
    return signAndJustify(context, v0, value.signum() < 0);
  }

  static Seq g(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    int precision = 6;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
      if (precision == 0) {
        precision = 1;
      }
    }
    FloatLayout layout = value.generalLayout(precision);
    char[] exp = layout.getExponent();
    if (exp != null) {
      precision -= (layout.getExponentRounded() + 1);
    } else {
      --precision;
    }
    Seq v0 = addZeros(layout.getMantissa(), precision);
    if (precision == 0 && context.hasFlag(Flag.ALTERNATE)) {
      v0 = v0.append(DOT);
    }
    if (exp != null) {
      v0 = v0.append(E);
      v0 = v0.append(Seq.forArray(exp));
    }

    return signAndJustify(context, v0, value.signum() < 0);
  }

  static Seq a(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    // TODO
    return Seq.empty();
  }

  static Seq c(FormatContext context, FormatTraits value) {
    return spaceJustify(context, Seq.singleChar((char) value.asInt()));
  }

  static Seq s(FormatContext context, FormatTraits value) {
    String s = value.asString();
    int precision;
    if (context.isPrecisionSet() && (precision = context.getPrecision()) < s.length()) {
      s = s.substring(0, precision);
    }
    return spaceJustify(context, Seq.wrap(s));
  }
}
