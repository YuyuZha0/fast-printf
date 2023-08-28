package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.util.Utils;

import java.lang.reflect.Array;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public final class ObjectTraits implements FormatTraits {

  private final Object value;

  public ObjectTraits(Object value) {
    this.value = value;
  }

  private static String arrayToString(Object array, Class<?> componentType) {
    if (componentType == byte.class) {
      Base64.Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString((byte[]) array);
    }
    if (componentType == char.class) {
      return String.valueOf((char[]) array);
    }
    int length = Array.getLength(array);
    if (length == 0) {
      return "[]";
    }
    if (componentType == boolean.class) {
      return Arrays.toString((boolean[]) array);
    }
    if (componentType == short.class) {
      return Arrays.toString((short[]) array);
    }
    if (componentType == int.class) {
      return Arrays.toString((int[]) array);
    }
    if (componentType == long.class) {
      return Arrays.toString((long[]) array);
    }
    if (componentType == float.class) {
      return Arrays.toString((float[]) array);
    }
    if (componentType == double.class) {
      return Arrays.toString((double[]) array);
    }

    return Utils.join(new StringBuilder().append('['), ", ", array).append(']').toString();
  }

  @Override
  public String asString() {
    Class<?> type = value.getClass();
    if (type.isArray()) {
      return arrayToString(value, type.getComponentType());
    }
    return value.toString();
  }

  @Override
  public IntForm asIntForm() {
    if (value instanceof Number) {
      return IntForm.valueOf(((Number) value).longValue());
    }
    throw new PrintfException("%s is not a number", value);
  }

  @Override
  public FloatForm asFloatForm() {
    if (value instanceof Number) {
      return FloatForm.valueOf(((Number) value).doubleValue());
    }
    throw new PrintfException("%s is not a number", value);
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    if (value instanceof Date) {
      return ((Date) value).toInstant();
    }
    if (value instanceof Calendar) {
      return ((Calendar) value).toInstant();
    }
    if (value instanceof Number) {
      return Utils.longToInstant(((Number) value).longValue());
    }
    throw new PrintfException("%s cannot be converted to TemporalAccessor", value);
  }

  @Override
  public int asInt() {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    throw new PrintfException("%s cannot be converted to int", value);
  }

  @Override
  public Object value() {
    return value;
  }
}
