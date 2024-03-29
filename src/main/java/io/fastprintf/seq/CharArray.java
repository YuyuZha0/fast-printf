package io.fastprintf.seq;

import io.fastprintf.util.Utils;

import java.io.IOException;
import java.util.Arrays;

public final class CharArray implements SimpleSeq {

  private final char[] ch;
  private final int start;
  private final int length;
  private int _hash;

  CharArray(char[] _ch, int _start, int _length, boolean copy) {
    if (copy) {
      ch = new char[_length];
      start = 0;
      length = _length;
      System.arraycopy(_ch, _start, ch, 0, _length);
    } else {
      ch = _ch;
      start = _start;
      length = _length;
    }
    _hash = 0;
  }

  @Override
  public int hashCode() {
    if (_hash == 0) {
      // Same hash code algorithm as used for String
      // s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
      for (int i = start; i < start + length; i++) {
        _hash = 31 * _hash + ch[i];
      }
    }
    return _hash;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    return ch[start + index];
  }

  @Override
  public CharArray subSequence(int start, int end) {
    return new CharArray(ch, this.start + start, end - start, false);
  }

  @Override
  public String toString() {
    return String.valueOf(ch, start, length);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof CharArray) {
      CharArray cha = (CharArray) obj;
      return Arrays.equals(ch, cha.ch);
    }
    return false;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    for (int i = start; i < start + length; i++) {
      appendable.append(ch[i]);
    }
  }

  @Override
  public void appendTo(StringBuilder sb) {
    sb.append(ch, start, length);
  }

  @Override
  public CharArray upperCase() {
    char[] chars = Arrays.copyOfRange(ch, start, start + length);
    if (Utils.toUpperCase(chars)) {
      return new CharArray(chars, 0, length, false);
    }
    return this;
  }

  @Override
  public boolean isEmpty() {
    return length == 0;
  }

  @Override
  public int indexOf(char c) {
    int start = this.start;
    int length = this.length;
    for (int i = 0; i < length; ++i) {
      if (ch[start + i] == c) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }
}
