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

  /*
   * This overload exists purely as a performance optimization to avoid object allocation.
   *
   * The previous implementation in `Utils.longToInstant` was:
   *   Preconditions.checkArgument(value >= 0, "..." + value);
   *
   * Refactoring this to use our standard formatting would result in:
   *   Preconditions.checkArgument(value >= 0, "...", value); // 'value' is a primitive long
   *
   * This second version is cleaner, but it forces the JVM to auto-box the primitive `long` into
   * a `new Long()` object to fit into the `Object...` varargs array of the generic
   * `checkArgument` method.
   *
   * By providing this dedicated `checkLongArgument` overload, we pass the primitive `long`
   * directly, completely avoiding that object allocation and the resulting GC pressure. For a
   * performance-sensitive library, eliminating these small, frequent allocations is critical.
   */
  public static void checkLongArgument(boolean condition, String message, long value) {
    if (!condition) {
      throw new IllegalArgumentException(Utils.lenientFormat(message, value));
    }
  }

  public static void checkPositionIndex(int index, int size) {
    if (index < 0 || index >= size) {
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
