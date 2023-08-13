package org.fastprintf.traits;

import org.fastprintf.PrintfException;
import org.fastprintf.number.FloatForm;
import org.fastprintf.number.IntForm;
import org.fastprintf.util.Utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Base64;

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
    throw new PrintfException(value.getClass().getName() + " is not a number");
  }

  @Override
  public FloatForm asFloatForm() {
    if (value instanceof Number) {
      return FloatForm.valueOf(((Number) value).doubleValue());
    }
    throw new PrintfException(value.getClass().getName() + " is not a number");
  }

  @Override
  public int asInt() {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    throw new PrintfException(value + " cannot be converted to int");
  }

  @Override
  public Object value() {
    return value;
  }
}
