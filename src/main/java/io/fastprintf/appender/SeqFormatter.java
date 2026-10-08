package io.fastprintf.appender;

import io.fastprintf.Flag;
import io.fastprintf.FormatContext;
import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.FloatLayout;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.RefSlot;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

public final class SeqFormatter {

  private SeqFormatter() {
    throw new IllegalStateException();
  }

  private static int precision(FormatContext context, int defaultValue) {
    return context.isPrecisionSet() ? context.getPrecision() : defaultValue;
  }

  private static Seq spaceJustify(FormatContext context, Seq value) {
    int padding = context.getWidth() - value.length();
    if (padding <= 0) {
      return value;
    }
    Seq spaces = Seq.repeated(' ', padding);
    return context.hasFlag(Flag.LEFT_JUSTIFY) ? value.append(spaces) : value.prepend(spaces);
  }

  private static char sign(FormatContext context, boolean negative) {
    if (negative) return '-';
    if (context.hasFlag(Flag.PLUS)) return '+';
    if (context.hasFlag(Flag.LEADING_SPACE)) return ' ';
    return 0;
  }

  private static Seq signed(Seq value, char sign) {
    return sign == 0 ? value : value.prepend(Seq.ch(sign));
  }

  /** Orders every numeric field as spaces, sign/base prefix, zeros, digits, trailing spaces. */
  private static Seq number(
      FormatContext context,
      Seq digits,
      char sign,
      String prefix,
      int minimumDigits,
      boolean allowZeroPadding) {
    int zeros = minimumDigits - digits.length();
    if (allowZeroPadding && context.hasFlag(Flag.ZERO_PAD) && !context.hasFlag(Flag.LEFT_JUSTIFY)) {
      zeros =
          Math.max(
              zeros, context.getWidth() - prefix.length() - (sign == 0 ? 0 : 1) - digits.length());
    }
    if (zeros > 0) {
      digits = digits.prepend(Seq.repeated('0', zeros));
    }
    if (!prefix.isEmpty()) {
      digits = digits.prepend(Seq.wrap(prefix));
    }
    return spaceJustify(context, signed(digits, sign));
  }

  private static Seq signAndJustify(FormatContext context, Seq digits, boolean negative) {
    return number(context, digits, sign(context, negative), "", 0, true);
  }

  static Seq d(FormatContext context, IntForm value) {
    int signum = value.signum();
    Seq digits =
        signum == 0 && context.getPrecision() == 0
            ? Seq.empty()
            : Seq.wrap(value.toDecimalString());
    return number(
        context,
        digits,
        sign(context, signum < 0),
        "",
        precision(context, 1),
        !context.isPrecisionSet());
  }

  static Seq o(FormatContext context, IntForm value) {
    Seq digits = integerDigits(context, value, IntForm::toOctalString);
    int minimumDigits = precision(context, 1);
    // Octal '#' is a precision requirement, not a separate prefix. Even %#.0o prints zero.
    if (context.hasFlag(Flag.ALTERNATE) && (digits.isEmpty() || digits.charAt(0) != '0')) {
      minimumDigits = Math.max(minimumDigits, digits.length() + 1);
    }
    return number(context, digits, (char) 0, "", minimumDigits, !context.isPrecisionSet());
  }

  static Seq x(FormatContext context, IntForm value) {
    return x(context, value, false);
  }

  static Seq x(FormatContext context, IntForm value, boolean uppercase) {
    Seq digits = integerDigits(context, value, IntForm::toHexString);
    if (uppercase) digits = digits.upperCase();
    String prefix =
        context.hasFlag(Flag.ALTERNATE) && value.signum() != 0 ? (uppercase ? "0X" : "0x") : "";
    return number(
        context, digits, (char) 0, prefix, precision(context, 1), !context.isPrecisionSet());
  }

  static Seq u(FormatContext context, IntForm value) {
    return number(
        context,
        integerDigits(context, value, IntForm::toUnsignedDecimalString),
        (char) 0,
        "",
        precision(context, 1),
        !context.isPrecisionSet());
  }

  private static Seq integerDigits(
      FormatContext context, IntForm value, Function<IntForm, String> convert) {
    return value.signum() == 0 && context.getPrecision() == 0
        ? Seq.empty()
        : Seq.wrap(convert.apply(value));
  }

  private static Seq nanOrInfinity(FormatContext context, FloatForm value, boolean uppercase) {
    Seq text =
        Seq.wrap(
            value.isNaN() ? (uppercase ? "NAN" : "NaN") : (uppercase ? "INFINITY" : "Infinity"));
    if (!value.isNaN()) {
      text = signed(text, sign(context, value.isNegative()));
    }
    // Non-finite values use spaces even when zero padding was requested.
    return spaceJustify(context, text);
  }

  static Seq f(FormatContext context, FloatForm value) {
    return f(context, value, false);
  }

  static Seq f(FormatContext context, FloatForm value, boolean uppercase) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value, uppercase);
    }
    int precision = precision(context, 6);
    FloatLayout layout = value.decimalLayout(precision);
    Seq mantissa =
        formatFractionalPart(layout.getMantissa(), precision, context.hasFlag(Flag.ALTERNATE));
    return signAndJustify(context, mantissa, value.isNegative());
  }

  private static Seq formatFractionalPart(
      Seq mantissa, int precision, boolean reserveDotWhenNoFraction) {
    int dot = mantissa.indexOf('.');
    if (precision == 0 && dot == Seq.INDEX_NOT_FOUND) {
      return reserveDotWhenNoFraction ? mantissa.append(Seq.ch('.')) : mantissa;
    }
    int outPrecision = 0;
    if (dot >= 0) {
      outPrecision = mantissa.length() - (dot + 1);
    }
    // Trust that the FloatForm layer has already rounded. If the mantissa has
    // more precision than requested, we do not truncate it here.
    if (outPrecision >= precision) {
      return mantissa;
    }
    if (dot < 0) {
      mantissa = mantissa.append(Seq.ch('.'));
    }
    return mantissa.append(Seq.repeated('0', precision - outPrecision));
  }

  static Seq e(FormatContext context, FloatForm value) {
    return e(context, value, false);
  }

  static Seq e(FormatContext context, FloatForm value, boolean uppercase) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value, uppercase);
    }
    int precision = precision(context, 6);
    FloatLayout layout = value.scientificLayout(precision);
    Seq v0 = formatFractionalPart(layout.getMantissa(), precision, context.hasFlag(Flag.ALTERNATE));
    v0 = v0.append(Seq.ch(uppercase ? 'E' : 'e'));
    v0 = v0.append(layout.getExponent());
    return signAndJustify(context, v0, value.isNegative());
  }

  /** Helper for %g that strips trailing zeros from the fractional part. */
  private static Seq stripTrailingZeros(Seq mantissa) {
    int dotIndex = mantissa.indexOf('.');
    if (dotIndex == Seq.INDEX_NOT_FOUND) {
      return mantissa; // No fractional part, nothing to strip
    }

    int lastCharIndex = mantissa.length() - 1;
    // Find last non-zero character in the fractional part
    while (lastCharIndex > dotIndex && mantissa.charAt(lastCharIndex) == '0') {
      lastCharIndex--;
    }

    // If the last non-zero character is the dot itself, strip the dot too
    if (lastCharIndex == dotIndex) {
      lastCharIndex--;
    }

    return mantissa.subSequence(0, lastCharIndex + 1);
  }

  /** Helper for %#g that pads with trailing zeros to meet the specified precision. */
  private static Seq padToPrecision(Seq mantissa, int precision) {
    int length = mantissa.length();
    int dot = mantissa.indexOf('.');
    int firstSignificant = 0;
    // Only leading zeros need inspection. Avoid a charAt traversal for every digit of a rope.
    while (firstSignificant < length) {
      char digit = mantissa.charAt(firstSignificant);
      if (digit != '0' && digit != '.') break;
      firstSignificant++;
    }
    int significantDigits =
        firstSignificant == length
            ? 1
            : length - firstSignificant - (dot >= firstSignificant ? 1 : 0);
    int zerosToPad = precision - significantDigits;
    if (dot == Seq.INDEX_NOT_FOUND) {
      mantissa = mantissa.append(Seq.ch('.'));
    }

    if (zerosToPad > 0) {
      mantissa = mantissa.append(Seq.repeated('0', zerosToPad));
    }
    return mantissa;
  }

  static Seq g(FormatContext context, FloatForm value) {
    return g(context, value, false);
  }

  static Seq g(FormatContext context, FloatForm value, boolean uppercase) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value, uppercase);
    }
    int precision = Math.max(1, precision(context, 6));
    FloatLayout layout = value.generalLayout(precision);
    Seq mantissa =
        context.hasFlag(Flag.ALTERNATE)
            ? padToPrecision(layout.getMantissa(), precision)
            : stripTrailingZeros(layout.getMantissa());
    if (layout.getExponent() != null) {
      mantissa = mantissa.append(Seq.ch(uppercase ? 'E' : 'e')).append(layout.getExponent());
    }
    return signAndJustify(context, mantissa, value.isNegative());
  }

  static Seq a(FormatContext context, FloatForm value) {
    return a(context, value, false);
  }

  static Seq a(FormatContext context, FloatForm value, boolean uppercase) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value, uppercase);
    }
    // Keep the established Java-compatible convention: %.0a uses one fractional digit.
    int precision = Math.max(1, precision(context, 13));
    FloatLayout layout = value.hexLayout(precision);
    Seq mantissa = layout.getMantissa();
    if (uppercase) mantissa = mantissa.upperCase();
    if (context.isPrecisionSet()) {
      mantissa = formatFractionalPart(mantissa, precision, context.hasFlag(Flag.ALTERNATE));
    }
    Seq digits = mantissa.append(Seq.ch(uppercase ? 'P' : 'p')).append(layout.getExponent());
    return number(
        context, digits, sign(context, value.isNegative()), uppercase ? "0X" : "0x", 0, true);
  }

  static Seq c(FormatContext context, FormatTraits value) {
    return spaceJustify(context, Seq.ch(value.asChar()));
  }

  static Seq s(FormatContext context, FormatTraits value) {
    Seq seq = value.asSeq();
    int precision;
    if (context.isPrecisionSet() && (precision = context.getPrecision()) < seq.length()) {
      seq = seq.subSequence(0, precision);
    }
    return spaceJustify(context, seq);
  }

  static Seq p(FormatContext context, FormatTraits traits) {
    RefSlot slot = traits.ref();
    if (slot.isPrimitive()) {
      throw new PrintfException("The '%p' specifier cannot be used with primitive types.");
    }
    Object value = slot.get();

    if (value == null) {
      return spaceJustify(context, Seq.wrap("null"));
    }

    Seq seq = Seq.wrap(Integer.toHexString(System.identityHashCode(value)));
    seq = seq.prepend(Seq.ch('@'));
    seq = seq.prepend(Seq.wrap(value.getClass().getName()));

    return spaceJustify(context, seq);
  }

  private static DateTimeFormatter bestDefaultFormatterOrThrow(TemporalAccessor ta) {
    if (ta instanceof OffsetDateTime || ta instanceof ZonedDateTime) {
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    } else if (ta instanceof LocalDateTime) {
      return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    } else if (ta instanceof LocalDate) {
      return DateTimeFormatter.ISO_LOCAL_DATE;
    } else {
      throw new PrintfException(
          "No default DateTimeFormatter for type: %s", ta.getClass().getName());
    }
  }

  static Seq t(FormatContext context, FormatTraits traits) {
    DateTimeFormatter formatter = context.getDateTimeFormatter();
    TemporalAccessor temporalAccessor = traits.asTemporalAccessor();

    if (formatter == null) {
      if (temporalAccessor instanceof Instant) {
        temporalAccessor = ((Instant) temporalAccessor).atZone(ZoneId.systemDefault());
        formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
      } else {
        formatter = bestDefaultFormatterOrThrow(temporalAccessor);
      }
    }
    Seq seq = Seq.wrap(formatter.format(temporalAccessor));
    return spaceJustify(context, seq);
  }
}
