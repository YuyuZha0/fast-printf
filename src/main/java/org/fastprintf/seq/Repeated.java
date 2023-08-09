package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;
import java.util.Arrays;

public final class Repeated implements Seq {

  private static final Repeated[] SINGLE_CHAR_REPEATED = new Repeated[128];

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

  static Repeated ofSingleChar(char c) {
    if (c < SINGLE_CHAR_REPEATED.length) {
      return SINGLE_CHAR_REPEATED[c];
    }
    return new Repeated(c, 1);
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
    if (index < 0 || index >= count) throw new IndexOutOfBoundsException();
    return c;
  }

  @Override
  public Seq subSequence(int start, int end) {
    if (start < 0 || end > count || start > end) throw new IllegalArgumentException();
    if (start == end) return Seq.empty();
    return new Repeated(c, end - start);
  }

  @Override
  public String toString() {
    return String.valueOf(toCharArray());
  }

  private char[] toCharArray() {
    char[] chars = new char[count];
    Arrays.fill(chars, c);
    return chars;
  }

  @Override
  public Seq upperCase() {
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
    for (int i = 0; i < count; i++) {
      sb.append(c);
    }
  }

  @Override
  public int indexOf(char c) {
    return this.c == c ? 0 : INDEX_NOT_FOUND;
  }
}
