package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import io.fastprintf.util.Utils;
import java.io.IOException;

final class StrView implements AtomicSeq {

  private final String str;
  private final int start;
  private final int length;
  private final boolean upperCase;

  StrView(String str, int start, int length) {
    this(str, start, length, false);
  }

  private StrView(String str, int start, int length, boolean upperCase) {
    this.str = str;
    this.start = start;
    this.length = length;
    this.upperCase = upperCase;
  }

  @Override
  public AtomicSeq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) {
      return Seq.empty();
    }
    if (end == start + 1) {
      char c = str.charAt(this.start + start);
      return Repeated.ofSingleChar(upperCase ? Utils.toUpperCase(c) : c);
    }
    return new StrView(str, this.start + start, end - start, upperCase);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, length);
    char ch = str.charAt(start + index);
    return upperCase ? Utils.toUpperCase(ch) : ch;
  }

  @Override
  public String toString() {
    if (upperCase) {
      char[] chars = toCharArray();
      Utils.toUpperCase(chars);
      return String.valueOf(chars);
    }
    return str.substring(start, start + length);
  }

  private int fastIndexOf(char c) {
    int index = str.indexOf(c, start);
    if (index == -1 || index >= start + length) {
      return INDEX_NOT_FOUND;
    }
    return index - start;
  }

  @Override
  public int indexOf(char c) {
    if (upperCase) {
      if (Utils.isLowerCase(c)) {
        return INDEX_NOT_FOUND;
      } else if (!Utils.isUpperCase(c)) {
        return fastIndexOf(c);
      }
      for (int i = 0; i < length; i++) {
        char ch = str.charAt(start + i);
        if (Utils.toUpperCase(ch) == c) {
          return i;
        }
      }
      return INDEX_NOT_FOUND;
    } else {
      return fastIndexOf(c);
    }
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    if (upperCase) {
      for (int i = start; i < start + length; i++) {
        appendable.append(Utils.toUpperCase(str.charAt(i)));
      }
      return;
    }
    appendable.append(str, start, start + length);
  }

  private char[] toCharArray() {
    char[] chars = new char[length];
    str.getChars(start, start + length, chars, 0);
    return chars;
  }

  @Override
  public void appendTo(StringBuilder sb) {
    if (upperCase) {
      if (length < ARRAY_APPEND_THRESHOLD) {
        sb.ensureCapacity(sb.length() + length);
        for (int i = start; i < start + length; i++) {
          sb.append(Utils.toUpperCase(str.charAt(i)));
        }
      } else {
        char[] chars = toCharArray();
        Utils.toUpperCase(chars);
        sb.append(chars);
      }
      return;
    }
    if (start == 0 && length == str.length()) {
      sb.append(str);
      return;
    }

    // On JDK 8, this substring append is slow due to a loop-based implementation, but we do not
    // specialize for that here.
    sb.append(str, start, start + length);
  }

  @Override
  public AtomicSeq upperCase() {
    if (upperCase) {
      return this;
    }
    return new StrView(str, start, length, true);
  }
}
