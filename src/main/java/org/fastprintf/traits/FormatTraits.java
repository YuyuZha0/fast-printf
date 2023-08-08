package org.fastprintf.traits;

import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;

public interface FormatTraits {

  default boolean isNull() {
    return false;
  }

  IntForm asIntForm();

  FloatForm asFloatForm();

  String asString();

  int asInt();

  default char asChar() {
    return (char) asInt();
  }

  Object value();
}
