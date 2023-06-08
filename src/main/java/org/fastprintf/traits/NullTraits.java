package org.fastprintf.traits;

import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;

public final class NullTraits implements FormatTraits {

  @Override
  public boolean isNegative() {
    return false;
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public Seq seqForSpecifier(Specifier specifier, FormatContext context) {
    return Seq.wrap(asCharSequence());
  }

  @Override
  public long asLong() {
    return 0;
  }

  @Override
  public long asUnsignedLong() {
    return 0;
  }

  @Override
  public int asInt() {
    return 0;
  }

  @Override
  public double asDouble() {
    return 0D;
  }

  @Override
  public String asCharSequence() {
    return "null";
  }
}
