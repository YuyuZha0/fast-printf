package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import io.fastprintf.util.Utils;
import java.io.IOException;
import java.util.Arrays;

final class CharArray implements AtomicSeq {

  private final char[] ch;
  private final int start;
  private final int length;
  private final boolean upperCase;
  private int hash;

  private CharArray(char[] ch, int start, int length, boolean upperCase) {
    this.ch = ch;
    this.start = start;
    this.length = length;
    this.upperCase = upperCase;
    this.hash = 0;
  }

  /**
   * Creates a zero-copy "view" of the given character array.
   *
   * <p><b>Warning:</b> This method does not create a copy of the array. Any subsequent
   * modifications to the backing {@code char[]} will be reflected in this sequence. Use this method
   * for performance-critical code where the source array is known to be immutable.
   *
   * @param ch the source character array.
   * @param start the starting index in the array.
   * @param length the number of characters to include.
   * @return a new {@code CharArray} that shares the input array.
   */
  static CharArray wrap(char[] ch, int start, int length) {
    return new CharArray(ch, start, length, false);
  }

  /**
   * Creates a new {@code CharArray} by copying the specified region of the given character array.
   *
   * <p>This method provides safety by creating a defensive copy of the data. Subsequent
   * modifications to the original array will not affect the returned sequence.
   *
   * @param ch the source character array.
   * @param start the starting index in the array.
   * @param length the number of characters to copy.
   * @return a new {@code CharArray} with its own copy of the character data.
   */
  static CharArray create(char[] ch, int start, int length) {
    char[] nch = new char[length];
    System.arraycopy(ch, start, nch, 0, length);
    return new CharArray(nch, 0, length, false);
  }

  @Override
  public int hashCode() {
    int hash = this.hash;
    if (hash == 0) {
      // Same hash code algorithm as used for String
      // s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
      if (upperCase) {
        for (int i = start; i < start + length; i++) {
          hash = 31 * hash + Utils.toUpperCase(ch[i]);
        }
      } else {
        for (int i = start; i < start + length; i++) {
          hash = 31 * hash + ch[i];
        }
      }
      this.hash = hash;
    }
    return hash;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, length);
    char c = ch[start + index];
    return upperCase ? Utils.toUpperCase(c) : c;
  }

  @Override
  public AtomicSeq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) {
      return Seq.empty();
    }
    if (end == start + 1) {
      char c = ch[this.start + start];
      return Repeated.ofSingleChar(upperCase ? Utils.toUpperCase(c) : c);
    }
    return new CharArray(ch, this.start + start, end - start, upperCase);
  }

  private void appendUpperCaseTo(StringBuilder sb) {
    if (length < ARRAY_APPEND_THRESHOLD) {
      sb.ensureCapacity(sb.length() + length);
      for (int i = start; i < start + length; i++) {
        sb.append(Utils.toUpperCase(ch[i]));
      }
    }else {
        char[] copy = Arrays.copyOfRange(ch, start, start + length);
        Utils.toUpperCase(copy);
        sb.append(copy);
    }
  }

  @Override
  public String toString() {
    if (upperCase) {
      StringBuilder sb = new StringBuilder(length);
      appendUpperCaseTo(sb);
      return sb.toString();
    }
    return String.valueOf(ch, start, length);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof CharArray) {
      CharArray cha = (CharArray) obj;
      if (this.length != cha.length) {
        return false;
      }
      for (int i = 0; i < length; i++) {
        if (this.charAt(i) != cha.charAt(i)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    if (upperCase) {
      for (int i = start; i < start + length; i++) {
        appendable.append(Utils.toUpperCase(ch[i]));
      }
    } else {
      for (int i = start; i < start + length; i++) {
        appendable.append(ch[i]);
      }
    }
  }

  @Override
  public void appendTo(StringBuilder sb) {
    if (upperCase) {
      appendUpperCaseTo(sb);
      return;
    }
    sb.append(ch, start, length);
  }

  @Override
  public CharArray upperCase() {
    if (upperCase) {
      return this;
    } else {
      return new CharArray(ch, start, length, true);
    }
  }

  @Override
  public boolean isEmpty() {
    return length == 0;
  }

  @Override
  public int indexOf(char c) {
    int start = this.start;
    int length = this.length;
    if (upperCase) {
      if (Utils.isLowerCase(c)) {
        return INDEX_NOT_FOUND;
      }
      for (int i = 0; i < length; ++i) {
        if (Utils.toUpperCase(ch[start + i]) == c) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < length; ++i) {
        if (ch[start + i] == c) {
          return i;
        }
      }
    }
    return INDEX_NOT_FOUND;
  }
}
