package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

public interface FormatTraits {

  default boolean isNull() {
    return false;
  }

  IntFamily asIntFamily();

  FloatFamily asFloatFamily();

  String asString();

  int asInt();

  default char asChar() {
    return (char) asInt();
  }

  Object value();
}
