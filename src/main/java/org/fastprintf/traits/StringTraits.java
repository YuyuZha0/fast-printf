package org.fastprintf.traits;

import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;

public final class StringTraits implements FormatTraits {

  private final Object value;

  public StringTraits(Object value) {
    this.value = value;
  }

  @Override
  public boolean isNegative() {
    return false;
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public Seq seqForSpecifier(Specifier specifier, FormatContext context) {
    return Seq.wrap(asString());
  }

  @Override
  public int asInt() {
    return 0;
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
  public double asDouble() {
    return 0D;
  }

  @Override
  public String asString() {
    return value.toString();
  }
}
