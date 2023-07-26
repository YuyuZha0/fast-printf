package org.fastprintf;

import org.fastprintf.traits.FormatTraits;

import java.math.BigDecimal;
import java.math.BigInteger;
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

  static Args create() {
    return new ArgsImpl();
  }

  List<FormatTraits> asList();

  Args putNull();

  Args putBoolean(boolean value);

  Args putChar(char value);

  Args putByte(byte value);

  Args putShort(short value);

  Args putInt(int value);

  Args putLong(long value);

  Args putFloat(float value);

  Args putDouble(double value);

  default Args putString(String value) {
    return putCharSequence(value);
  }

  Args putCharSequence(CharSequence value);

  Args putBigInteger(BigInteger value);

  Args putBigDecimal(BigDecimal value);

  Args putAny(Object value);

  default Args put(Object value) {
    if (value == null) {
      return putNull();
    } else if (value instanceof Boolean) {
      return putBoolean((Boolean) value);
    } else if (value instanceof Character) {
      return putChar((Character) value);
    } else if (value instanceof Byte) {
      return putByte((Byte) value);
    } else if (value instanceof Short) {
      return putShort((Short) value);
    } else if (value instanceof Integer) {
      return putInt((Integer) value);
    } else if (value instanceof Long) {
      return putLong((Long) value);
    } else if (value instanceof Float) {
      return putFloat((Float) value);
    } else if (value instanceof Double) {
      return putDouble((Double) value);
    } else if (value instanceof CharSequence) {
      return putCharSequence((CharSequence) value);
    } else if (value instanceof BigInteger) {
      return putBigInteger((BigInteger) value);
    } else if (value instanceof BigDecimal) {
      return putBigDecimal((BigDecimal) value);
    } else {
      return putAny(value);
    }
  }
}
