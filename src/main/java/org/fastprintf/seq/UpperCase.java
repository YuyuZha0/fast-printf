package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;

public final class UpperCase implements Seq {

  private final CharSequence cs;

  UpperCase(CharSequence cs) {
    this.cs = cs;
  }

  @Override
  public int length() {
    return cs.length();
  }

  @Override
  public char charAt(int index) {
    return Utils.toUpperCase(cs.charAt(index));
  }

  @Override
  public UpperCase subSequence(int start, int end) {
    return new UpperCase(cs.subSequence(start, end));
  }

  @Override
  public String toString() {
    return Utils.toUpperCase(cs.toString());
  }

  @Override
  public Seq upperCase() {
    return this;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    appendable.append(toString());
  }

  @Override
  public void appendTo(StringBuilder sb) {
    if (cs instanceof String) {
      char[] chars = ((String) cs).toCharArray();
      Utils.toUpperCase(chars);
      sb.append(chars);
    } else {
      int length = cs.length();
      for (int i = 0; i < length; i++) {
        sb.append(Utils.toUpperCase(cs.charAt(i)));
      }
    }
  }

  @Override
  public int indexOf(char c) {
    if (Utils.isLowerCase(c)) {
      return Utils.INDEX_NOT_FOUND;
    }
    int length = cs.length();
    for (int i = 0; i < length; i++) {
      if (Utils.toUpperCase(cs.charAt(i)) == c) {
        return i;
      }
    }
    return Utils.INDEX_NOT_FOUND;
  }
}
