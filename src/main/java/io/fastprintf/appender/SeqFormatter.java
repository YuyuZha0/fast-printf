package io.fastprintf.appender;

import io.fastprintf.Flag;
import io.fastprintf.FormatContext;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.FloatLayout;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

public final class SeqFormatter {

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
      v0 = v0.prepend(Seq.ch('-'));
    } else if (context.hasFlag(Flag.PLUS)) {
      v0 = v0.prepend(Seq.ch('+'));
    } else if (context.hasFlag(Flag.LEADING_SPACE)) {
      v0 = v0.prepend(Seq.ch(' '));
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

  static Seq d(FormatContext context, IntForm value) {
    int signum = value.signum();
    if (signum == 0 && context.getPrecision() == 0) {
      Seq v0 = context.hasFlag(Flag.PLUS) ? Seq.ch('+') : Seq.empty();
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

  static Seq o(FormatContext context, IntForm value) {
    return formatUnsignedInteger(context, value, IntForm::toOctalString, "0");
  }

  static Seq x(FormatContext context, IntForm value) {
    return formatUnsignedInteger(context, value, IntForm::toHexString, "0x");
  }

  static Seq u(FormatContext context, IntForm value) {
    return formatUnsignedInteger(context, value, IntForm::toUnsignedDecimalString, "");
  }

  private static Seq formatUnsignedInteger(
      FormatContext context, IntForm value, Function<IntForm, String> toString, String prefix) {
    int signum = value.signum();
    if (signum == 0 && context.getPrecision() == 0) {
      return spaceJustify(context, Seq.empty());
    }
    Seq v0 = Seq.wrap(toString.apply(value));
    int precision = 1;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
      if (context.hasFlag(Flag.ALTERNATE) && "0".equals(prefix)) {
        --precision;
      }
    } else if (context.hasFlag(Flag.ZERO_PAD)
        && context.isWidthSet()
        && !context.hasFlag(Flag.LEFT_JUSTIFY)) {
      precision = context.getWidth();
      if (context.hasFlag(Flag.ALTERNATE)) {
        precision -= prefix.length();
      }
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

  private static Seq nanOrInfinity(FormatContext context, FloatForm value) {
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

  static Seq f(FormatContext context, FloatForm value) {
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
      mantissa = mantissa.append(Seq.ch('.'));
    }
    return signAndJustify(context, mantissa, value.isNegative());
  }

  private static Seq addZeros(Seq mantissa, int precision) {
    int dot = mantissa.indexOf('.');
    int outPrecision = 0;
    if (dot >= 0) {
      outPrecision = mantissa.length() - (dot + 1);
    }
    if (outPrecision == precision) {
      return mantissa;
    }
    if (dot < 0) {
      mantissa = mantissa.append(Seq.ch('.'));
    }
    return mantissa.append(Seq.repeated('0', precision - outPrecision));
  }

  static Seq e(FormatContext context, FloatForm value) {
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
      v0 = v0.append(Seq.ch('.'));
    }
    v0 = v0.append(Seq.ch('e'));
    v0 = v0.append(layout.getExponent());
    return signAndJustify(context, v0, value.isNegative());
  }

  private static Seq truncateDotAndZero(Seq v0) {
    int length = v0.length();
    int index = length - 1;
    while (index >= 1 && v0.charAt(index) == '0') {
      --index;
    }
    if (index >= 1 && v0.charAt(index) == '.') {
      --index;
    }
    return v0.subSequence(0, index + 1);
  }

  static Seq g(FormatContext context, FloatForm value) {
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
    Seq v0 = truncateDotAndZero(layout.getMantissa());
    Seq exp = layout.getExponent();
    if (exp != null) {
      v0 = v0.append(Seq.ch('e'));
      v0 = v0.append(exp);
    }

    return signAndJustify(context, v0, value.isNegative());
  }

  static Seq a(FormatContext context, FloatForm value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    int precision = 0;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
      if (precision == 0) {
        precision = 1;
      }
    }
    FloatLayout layout = value.hexLayout(precision);
    Seq v0 = addZeros(layout.getMantissa(), precision);
    if (precision == 0 && context.hasFlag(Flag.ALTERNATE)) {
      v0 = v0.append(Seq.ch('.'));
    }
    v0 = v0.append(Seq.ch('p')).append(layout.getExponent());
    int signum = value.signum();
    if (context.hasFlag(Flag.ZERO_PAD) && !context.hasFlag(Flag.LEFT_JUSTIFY)) {
      int width = context.getWidth() - 2;
      if (signum < 0 || context.hasFlag(Flag.PLUS) || context.hasFlag(Flag.LEADING_SPACE)) {
        --width;
      }
      int len = v0.length();
      if (width > len) {
        v0 = Seq.repeated('0', width - len).append(v0);
      }
      v0 = v0.prepend(Seq.wrap("0x"));
      return sign(context, v0, signum < 0);
    }
    v0 = v0.prepend(Seq.wrap("0x"));
    v0 = sign(context, v0, signum < 0);
    return spaceJustify(context, v0);
  }

  static Seq c(FormatContext context, FormatTraits value) {
    return spaceJustify(context, Seq.ch(value.asChar()));
  }

  static Seq s(FormatContext context, FormatTraits value) {
    String s = value.asString();
    int precision;
    Seq seq;
    if (context.isPrecisionSet() && (precision = context.getPrecision()) < s.length()) {
      seq = Seq.wrap(s, 0, precision);
    } else {
      seq = Seq.wrap(s);
    }
    return spaceJustify(context, seq);
  }

  static Seq p(FormatContext context, FormatTraits traits) {
    Object value = traits.value();
    Seq seq = Seq.wrap(Integer.toHexString(System.identityHashCode(value)));
    seq = seq.prepend(Seq.ch('@'));
    seq = seq.prepend(Seq.wrap(value.getClass().getName()));
    return spaceJustify(context, seq);
  }

  static Seq t(FormatContext context, FormatTraits traits) {
    DateTimeFormatter formatter = context.getDateTimeFormatter();
    if (formatter == null) {
      formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    }
    TemporalAccessor temporalAccessor = traits.asTemporalAccessor();
    if (temporalAccessor instanceof Instant) {
      Instant instant = (Instant) temporalAccessor;
      temporalAccessor = instant.atZone(ZoneId.systemDefault());
    }
    Seq seq = Seq.wrap(formatter.format(temporalAccessor));
    return spaceJustify(context, seq);
  }
}
