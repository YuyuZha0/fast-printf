package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import io.fastprintf.util.Utils;
import java.io.IOException;

final class StrView implements AtomicSeq {
  private static final int LOOP_UNROLL_THRESHOLD = 16;

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
      if (length < LOOP_UNROLL_THRESHOLD) {
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
    // This method contains a performance-critical optimization to avoid the
    // notoriously slow, character-by-character loop in AbstractStringBuilder's
    // default `append(CharSequence, ...)` implementation. The goal is to always
    // use one of StringBuilder's fast-path overloads, which use native
    // System.arraycopy().

    // OPTIMIZATION 1: Handle the case where this view covers the entire backing string.
    // The `StringBuilder.append(String)` overload is the most direct and has the
    // simplest, fastest code path in the JDK.
    if (start == 0 && length == str.length()) {
      sb.append(str);
      return;
    }

    // HEURISTIC: Choose between the slow loop and an allocation-based fast path.
    // For very short substrings, the overhead of the slow `for`-loop inside
    // `AbstractStringBuilder` is acceptable and cheaper than allocating a new object.
    // For longer substrings, it is significantly faster to pay the cost of allocating
    // a temporary char[] array to force the use of the fast `append(char[])` overload,
    // which uses a single bulk memory copy. The threshold '16' is a common, empirically
    // determined crossover point for this trade-off.
    if (length < LOOP_UNROLL_THRESHOLD) {
      // For short strings, call the standard ranged append. This will unfortunately
      // use the slow, character-by-character loop because StringBuilder does not
      // override it with a fast path for CharSequence. However, the total overhead
      // is small enough to be acceptable.
      sb.append(str, start, start + length);
    } else {
      // For longer strings, explicitly create a temporary character array from our
      // substring. This forces the call to `StringBuilder.append(char[])`, which
      // is a highly optimized fast path that uses `System.arraycopy()`. The cost
      // of this one-time allocation is far outweighed by the performance gain of
      // avoiding the slow loop for a large number of characters.
      sb.append(toCharArray());
    }
  }

  @Override
  public AtomicSeq upperCase() {
    if (upperCase) {
      return this;
    }
    return new StrView(str, start, length, true);
  }
}
