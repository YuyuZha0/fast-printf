package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.time.temporal.TemporalAccessor;

public interface FormatTraits {

  default boolean isNull() {
    return false;
  }

  IntForm asIntForm();

  FloatForm asFloatForm();

  String asString();

  int asInt();

  default TemporalAccessor asTemporalAccessor() {
    throw new PrintfException("Cannot convert [%s] to TemporalAccessor", value());
  }

  default char asChar() {
    return (char) asInt();
  }

  Object value();
}
