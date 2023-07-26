package org.fastprintf.appender;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;
import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

import java.util.function.Function;

public final class SeqFormatter {

  private static final Seq PLUS = Seq.singleChar('+');
  private static final Seq MINUS = Seq.singleChar('-');
  private static final Seq SPACE = Seq.singleChar(' ');

  private SeqFormatter() {
    throw new IllegalStateException();
  }

  private static int width(FormatContext context, int defaultWidth) {
    return context.isWidthSet() ? context.getWidth() : defaultWidth;
  }

  private static Seq formatSignedInteger(
      FormatContext context, IntFamily value, Function<IntFamily, String> toString) {
    int signum = value.signum();
    if (signum == 0 && context.getPrecision() == 0) {
      Seq v0 = context.hasFlag(Flag.PLUS) ? PLUS : Seq.empty();
      return spaceJustify(context, v0);
    }
    Seq v0 = Seq.wrap(toString.apply(value));
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
    if (signum < 0) {
      v0 = v0.prepend(MINUS);
    } else if (context.hasFlag(Flag.PLUS)) {
      v0 = v0.prepend(PLUS);
    } else if (context.hasFlag(Flag.LEADING_SPACE)) {
      v0 = v0.prepend(SPACE);
    }
    return spaceJustify(context, v0);
  }

  private static Seq spaceJustify(FormatContext context, Seq v0) {
    int width = width(context, 0);
    if (width > v0.length()) {
      Seq pad = Seq.repeated(' ', width - v0.length());
      if (context.hasFlag(Flag.LEFT_JUSTIFY)) {
        v0 = v0.append(pad);
      } else {
        v0 = v0.prepend(pad);
      }
    }
    return v0;
  }

  private static Seq justify(FormatContext context, Seq v0) {
    int width = width(context, 0);
    if (width > v0.length()) {
      if (context.hasFlag(Flag.LEFT_JUSTIFY)) {
        v0 = v0.append(Seq.repeated(' ', width - v0.length()));
      } else {
        Seq pad = Seq.repeated(context.hasFlag(Flag.ZERO_PAD) ? '0' : ' ', width - v0.length());
        v0 = v0.prepend(pad);
      }
    }
    return v0;
  }

  static Seq d(FormatContext context, IntFamily value) {
    return formatSignedInteger(context, value, IntFamily::toDecimalString);
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

  static Seq f(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    return Seq.empty();
  }

  static Seq e(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    return Seq.empty();
  }

  static Seq g(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    return Seq.empty();
  }

  static Seq a(FormatContext context, FloatFamily value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    return Seq.empty();
  }

  static Seq c(FormatContext context, FormatTraits value) {
    return justify(context, Seq.singleChar((char) value.asInt()));
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
