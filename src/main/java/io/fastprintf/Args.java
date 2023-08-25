package io.fastprintf;

import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

public interface Args extends Iterable<FormatTraits> {

  static Args of(Object... values) {
    ArgsImpl args = new ArgsImpl(new ArrayList<>(values.length));
    for (Object value : values) {
      args.put(value);
    }
    return args;
  }

  static Args of(Iterable<?> values) {
    Preconditions.checkNotNull(values, "values");
    ArgsImpl args = new ArgsImpl();
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

  Args putChar(char value);

  Args putByte(byte value);

  Args putShort(short value);

  Args putInt(int value);

  Args putLong(long value);

  Args putFloat(float value);

  Args putDouble(double value);

  Args putDateTime(TemporalAccessor value);

  default Args putString(String value) {
    return putCharSequence(value);
  }

  Args putCharSequence(CharSequence value);

  Args putBigInteger(BigInteger value);

  Args putBigDecimal(BigDecimal value);

  Args put(Object value);
}
