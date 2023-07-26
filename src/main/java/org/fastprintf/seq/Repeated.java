package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;
import java.util.Arrays;

public final class Repeated implements Seq {

  private final char c;
  private final int count;

  Repeated(char c, int count) {
    if (count < 1) throw new IllegalArgumentException("count < 1");
    this.c = c;
    this.count = count;
  }

  @Override
  public int length() {
    return count;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index >= count) throw new IndexOutOfBoundsException();
    return c;
  }

  @Override
  public Seq subSequence(int start, int end) {
    if (start < 0 || end > count || start > end) throw new IllegalArgumentException();
    if (start == end) return EmptySeq.INSTANCE;
    return new Repeated(c, end - start);
  }

  @Override
  public String toString() {
    char[] chars = new char[count];
    Arrays.fill(chars, c);
    return String.valueOf(chars);
  }

  @Override
  public Seq upperCase() {
    if (Utils.isLowerCase(c)) {
      return new Repeated(Utils.toUpperCase(c), count);
    }
    return this;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    if (count > 1 && appendable instanceof StringBuilder) {
      char[] chars = new char[count];
      Arrays.fill(chars, c);
      ((StringBuilder) appendable).append(chars);
      return;
    }
    for (int i = 0; i < count; i++) {
      appendable.append(c);
    }
  }
}
