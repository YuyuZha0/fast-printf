package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import io.fastprintf.util.Utils;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

final class Repeated implements AtomicSeq {

  private static final Repeated[] SINGLE_CHAR_REPEATED = new Repeated[128];
  private static final RepeatedAppender APPENDER = findAppender();

  static {
    for (int i = 0; i < SINGLE_CHAR_REPEATED.length; i++) {
      SINGLE_CHAR_REPEATED[i] = new Repeated((char) i, 1);
    }
  }

  private final char c;
  private final int count;

  Repeated(char c, int count) {
    this.c = c;
    this.count = count;
  }

  private static RepeatedAppender findAppender() {
    try {
      // StringBuilder.repeat(int codePoint, int count) was added in JDK 21
      MethodHandle methodHandle =
          MethodHandles.publicLookup()
              .findVirtual(
                  StringBuilder.class,
                  "repeat",
                  MethodType.methodType(StringBuilder.class, int.class, int.class));
      return (sb, repeated) -> {
        try {
          StringBuilder result =
              (StringBuilder) methodHandle.invokeExact(sb, (int) repeated.c, repeated.count);
          assert result == sb;
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      };
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return Repeated::appendRepeated;
    }
  }

  static Repeated ofSingleChar(char c) {
    if (c < SINGLE_CHAR_REPEATED.length) {
      return SINGLE_CHAR_REPEATED[c];
    }
    return new Repeated(c, 1);
  }

  // Package-private for testability
  static void appendRepeated(StringBuilder sb, Repeated repeated) {
    int count = repeated.count;
    if (count == 0) return;
    if (count < ARRAY_APPEND_THRESHOLD) {
      char c = repeated.c;
      // This is an excellent micro-optimization. For the very common case of a single
      // character (count == 1), the single sb.append(c) call will handle its own
      // capacity check efficiently. We only need to pre-allocate for the loop when
      // count > 1 to prevent the possibility of multiple reallocations within that loop.
      if (count > 1) sb.ensureCapacity(sb.length() + count);
      for (int i = 0; i < count; i++) {
        sb.append(c);
      }
    } else {
      sb.append(repeated.toCharArray());
    }
  }

  @Override
  public int length() {
    return count;
  }

  @Override
  public boolean isEmpty() {
    return count == 0;
  }

  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, count);
    return c;
  }

  @Override
  public AtomicSeq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, count);
    if (start == end) return Seq.empty();
    return new Repeated(c, end - start);
  }

  @Override
  public String toString() {
    if (count == 0) {
      return "";
    }
    if (count == 1) {
      return String.valueOf(c);
    }
    return String.valueOf(toCharArray());
  }

  private char[] toCharArray() {
    char[] chars = new char[count];
    Arrays.fill(chars, c);
    return chars;
  }

  @Override
  public Repeated upperCase() {
    if (Utils.isLowerCase(c)) {
      char upperCase = Utils.toUpperCase(c);
      if (count == 1) return ofSingleChar(upperCase);
      return new Repeated(upperCase, count);
    }
    return this;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    for (int i = 0; i < count; i++) {
      appendable.append(c);
    }
  }

  @Override
  public void appendTo(StringBuilder sb) {
    APPENDER.append(sb, this);
  }

  @Override
  public int indexOf(char c) {
    return this.c == c && count > 0 ? 0 : INDEX_NOT_FOUND;
  }

  @FunctionalInterface
  private interface RepeatedAppender {
    void append(StringBuilder appendable, Repeated repeated);
  }
}
