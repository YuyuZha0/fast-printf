package org.fastprintf.traits;

import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;

abstract class AbstractTextTraits implements FormatTraits {

  @Override
  public final boolean isNegative() {
    return false;
  }

  @Override
  public final boolean isNull() {
    return false;
  }

  @Override
  public final int asInt() {
    return 0;
  }

  @Override
  public final long asLong() {
    return 0L;
  }

  @Override
  public final long asUnsignedLong() {
    return 0L;
  }

  @Override
  public final double asDouble() {
    return 0D;
  }

  @Override
  public Seq seqForSpecifier(Specifier specifier, FormatContext context) {
    if (specifier == Specifier.STRING) {
      return forString(context);
    }
    if (specifier == Specifier.CHARACTER) {
      return forCharacter(context);
    }
    if (specifier == Specifier.PERCENT_SIGN) {
      return Seq.singleChar('%');
    }
    return Seq.empty();
  }

  Seq forString(FormatContext context) {
    int precision = context.getPrecision();
    CharSequence cs = asCharSequence();
    int length = cs.length();
    if (precision == -1 || precision >= length) {
      return Seq.wrap(cs);
    } else {
      return Seq.wrap(cs.subSequence(0, precision));
    }
  }

  Seq forCharacter(FormatContext context) {
    CharSequence cs = asCharSequence();
    if (cs.length() == 0) {
      return Seq.empty();
    } else {
      return Seq.singleChar(cs.charAt(0));
    }
  }
}
