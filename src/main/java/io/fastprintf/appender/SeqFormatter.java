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
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    int precision = 6;
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
    }
    FloatLayout layout = value.scientificLayout(precision);
    Seq v0 = formatFractionalPart(layout.getMantissa(), precision, context.hasFlag(Flag.ALTERNATE));
    v0 = v0.append(Seq.ch('e'));
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
    // Count existing significant digits
    int sigDigits = 0;
    boolean nonZeroSeen = false;
    for (int i = 0; i < mantissa.length(); i++) {
      char c = mantissa.charAt(i);
      if (c >= '1' && c <= '9') nonZeroSeen = true;
      if (nonZeroSeen && c != '.') sigDigits++;
    }
    // Handle "0" or "0.0" which have one significant digit
    if (!nonZeroSeen && mantissa.indexOf('0') != -1) {
      sigDigits = 1;
    }

    int zerosToPad = precision - sigDigits;

    // Ensure decimal point exists
    if (mantissa.indexOf('.') == -1) {
      mantissa = mantissa.append(Seq.ch('.'));
    }

    if (zerosToPad > 0) {
      mantissa = mantissa.append(Seq.repeated('0', zerosToPad));
    }
    return mantissa;
  }

  /** Formats the mantissa for %g when it chooses decimal representation. */
  private static Seq formatGDecimal(FormatContext context, FloatLayout layout, int precision) {
    if (context.hasFlag(Flag.ALTERNATE)) {
      return padToPrecision(layout.getMantissa(), precision);
    } else {
      return stripTrailingZeros(layout.getMantissa());
    }
  }

  /** Formats the mantissa and exponent for %g when it chooses scientific representation. */
  private static Seq formatGScientific(FormatContext context, FloatLayout layout) {
    Seq mantissa = layout.getMantissa();
    if (!context.hasFlag(Flag.ALTERNATE)) {
      mantissa = stripTrailingZeros(mantissa);
    }
    return mantissa.append(Seq.ch('e')).append(layout.getExponent());
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
    Seq v0;

    if (layout.getExponent() == null) {
      // Use decimal ('f'-style) representation
      v0 = formatGDecimal(context, layout, precision);
    } else {
      // Use scientific ('e'-style) representation
      v0 = formatGScientific(context, layout);
    }

    return signAndJustify(context, v0, value.isNegative());
  }

  static Seq a(FormatContext context, FloatForm value) {
    if (value.isNaN() || value.isInfinite()) {
      return nanOrInfinity(context, value);
    }
    int precision =
        13; // Use 13 to let double to fallback to simpleHexLayout for full precision, and safe for
    // BigDecimal
    if (context.isPrecisionSet()) {
      precision = context.getPrecision();
      if (precision == 0) { // Special case: follow the exact behavior of Java's Formatter
        precision = 1;
      }
    }
    FloatLayout layout = value.hexLayout(precision); // Use 0 to get full precision
    Seq v0 = layout.getMantissa();
    if (context.isPrecisionSet()) {
      v0 = formatFractionalPart(v0, precision, context.hasFlag(Flag.ALTERNATE));
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

    // 2. Handle the null case explicitly
    if (value == null) {
      // The formatter decides on the representation for null, e.g., glibc's "(nil)"
      return spaceJustify(context, Seq.wrap("null"));
    }

    // 3. Perform all formatting logic here
    Seq seq = Seq.wrap(Integer.toHexString(System.identityHashCode(value)));
    seq = seq.prepend(Seq.ch('@'));
    seq = seq.prepend(Seq.wrap(value.getClass().getName()));

    // 4. Apply justification
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
