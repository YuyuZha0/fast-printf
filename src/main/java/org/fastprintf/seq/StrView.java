package org.fastprintf.seq;

import org.fastprintf.util.Preconditions;
import org.fastprintf.util.Utils;

import java.io.IOException;

public final class StrView implements SimpleSeq {

  private final String str;
  private final int start;
  private final int length;

  StrView(String str, int start, int length) {
    this.str = str;
    this.start = start;
    this.length = length;
  }

  @Override
  public SimpleSeq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) {
      return Seq.empty();
    }
    return new StrView(str, this.start + start, end - start);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    return str.charAt(start + index);
  }

  @Override
  public String toString() {
    return str.substring(start, start + length);
  }

  @Override
  public int indexOf(char c) {
    int index = str.indexOf(c, start);
    if (index == -1 || index >= start + length) {
      return INDEX_NOT_FOUND;
    }
    return index - start;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    appendable.append(str, start, start + length);
  }

  private char[] toCharArray() {
    char[] chars = new char[length];
    str.getChars(start, start + length, chars, 0);
    return chars;
  }

  @Override
  public void appendTo(StringBuilder sb) {
    sb.append(str, start, start + length);
  }

  @Override
  public SimpleSeq upperCase() {
    char[] chars = toCharArray();
    Utils.toUpperCase(chars);
    return Seq.forArray(chars);
  }
}
