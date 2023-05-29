package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;
import java.util.Arrays;

public final class CharArray implements Seq {

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
      if (length == cha.length) {
        int n = length;
        int i = start;
        int j = cha.start;
        while (n-- != 0) {
          if (ch[i++] != cha.ch[j++]) return false;
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    if (appendable instanceof StringBuilder) {
      StringBuilder sb = (StringBuilder) appendable;
      sb.append(ch, start, length);
    } else {
      for (int i = start; i < start + length; i++) {
        appendable.append(ch[i]);
      }
    }
  }

  @Override
  public Seq upperCase() {
    char[] chars = Arrays.copyOfRange(ch, start, start + length);
    Utils.toUpperCase(chars);
    return new CharArray(chars, 0, length, false);
  }
}
