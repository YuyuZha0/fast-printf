package org.fastprintf.traits;

import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;

public interface FormatTraits {

  boolean isNegative();

  boolean isNull();

  Seq seqForSpecifier(Specifier specifier, FormatContext context);

  default int asInt() {
    throw new UnsupportedOperationException("asInt");
  }

  default long asLong() {
    throw new UnsupportedOperationException("asLong");
  }

  default long asUnsignedLong() {
    throw new UnsupportedOperationException("asUnsignedLong");
  }

  default double asDouble() {
    throw new UnsupportedOperationException("asDouble");
  }

  default String asString() {
    throw new UnsupportedOperationException("asString");
  }
}
