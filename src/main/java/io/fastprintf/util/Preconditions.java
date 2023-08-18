package io.fastprintf.util;

public final class Preconditions {

  private Preconditions() {
    throw new IllegalStateException();
  }

  public static <T> T checkNotNull(T value, String name) {
    if (value == null) {
      String message = String.format("Argument '%s' cannot be null", name);
      throw new NullPointerException(message);
    }
    return value;
  }

  public static void checkArgument(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void checkPositionIndex(int index, int size) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
    }
  }

  public static void checkPositionIndexes(int start, int end, int size) {
    if (start < 0 || end < start || end > size) {
      throw new IndexOutOfBoundsException("start: " + start + ", end: " + end + ", size: " + size);
    }
  }
}
