package io.fastprintf.util;

public final class Preconditions {

  private Preconditions() {
    throw new IllegalStateException();
  }

  public static <T> T checkNotNull(T value, String name) {
    if (value == null) {
      throw new NullPointerException(Utils.lenientFormat("Argument '%s' cannot be null", name));
    }
    return value;
  }

  public static void checkArgument(boolean condition, String message, Object... args) {
    if (!condition) {
      throw new IllegalArgumentException(Utils.lenientFormat(message, args));
    }
  }

  public static void checkPositionIndex(int index, int size) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Utils.lenientFormat("index: %s, size: %s", index, size));
    }
  }

  public static void checkPositionIndexes(int start, int end, int size) {
    if (start < 0 || end < start || end > size) {
      throw new IndexOutOfBoundsException(
          Utils.lenientFormat("start: %s, end: %s, size: %s", start, end, size));
    }
  }
}
