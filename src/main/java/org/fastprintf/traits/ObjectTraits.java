package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

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
      return new String((char[]) array);
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
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < length; i++) {
      Object o = Array.get(array, i);
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(o);
    }
    sb.append(']');
    return sb.toString();
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
  public IntFamily asIntFamily() {
    throw new UnsupportedOperationException(value.getClass().getName() + " is not a number");
  }

  @Override
  public FloatFamily asFloatFamily() {
    throw new UnsupportedOperationException(value.getClass().getName() + " is not a number");
  }

  @Override
  public int asInt() {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    throw new UnsupportedOperationException();
  }
}
