package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import io.fastprintf.util.Utils;
import java.io.IOException;

final class StrView implements AtomicSeq {

  private final String str;
  private final int start;
  private final int length;

  StrView(String str, int start, int length) {
    this.str = str;
    this.start = start;
    this.length = length;
  }

  @Override
  public AtomicSeq subSequence(int start, int end) {
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
    if (length < 16) {
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
    char[] chars = toCharArray();
    Utils.toUpperCase(chars);
    return Seq.forArray(chars);
  }
}
