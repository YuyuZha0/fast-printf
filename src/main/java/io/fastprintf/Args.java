package io.fastprintf;

import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;

public interface Args extends Iterable<FormatTraits> {

  static Args of(Object... values) {
    ArgsImpl args = new ArgsImpl(values.length);
    for (Object value : values) {
      args.put(value);
    }
    return args;
  }

  static Args of(Iterable<?> values) {
    Preconditions.checkNotNull(values, "values");
    ArgsImpl args;
    if (values instanceof Collection) {
      args = new ArgsImpl(((Collection<?>) values).size());
    } else {
      args = new ArgsImpl();
    }
    for (Object value : values) {
      args.put(value);
    }
    return args;
  }

  static Args create() {
    return new ArgsImpl();
  }

  List<Object> values();

  Args putNull();

  Args putBoolean(boolean value);

  default Args putBooleanOrNull(Boolean value) {
    return value == null ? putNull() : putBoolean(value);
  }

  Args putChar(char value);

  default Args putCharOrNull(Character value) {
    return value == null ? putNull() : putChar(value);
  }

  Args putByte(byte value);

  default Args putByteOrNull(Byte value) {
    return value == null ? putNull() : putByte(value);
  }

  Args putShort(short value);

  default Args putShortOrNull(Short value) {
    return value == null ? putNull() : putShort(value);
  }

  Args putInt(int value);

  default Args putIntOrNull(Integer value) {
    return value == null ? putNull() : putInt(value);
  }

  Args putLong(long value);

  default Args putLongOrNull(Long value) {
    return value == null ? putNull() : putLong(value);
  }

  Args putFloat(float value);

  default Args putFloatOrNull(Float value) {
    return value == null ? putNull() : putFloat(value);
  }

  Args putDouble(double value);

  default Args putDoubleOrNull(Double value) {
    return value == null ? putNull() : putDouble(value);
  }

  Args putDateTime(TemporalAccessor value);

  default Args putString(String value) {
    return putCharSequence(value);
  }

  Args putCharSequence(CharSequence value);

  Args putBigInteger(BigInteger value);

  Args putBigDecimal(BigDecimal value);

  Args put(Object value);
}
