package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

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
