package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;

public final class CharSequenceView implements Seq {

  private final CharSequence cs;

  CharSequenceView(CharSequence cs) {
    this.cs = cs;
  }

  CharSequence unwrap() {
    return cs;
  }

  @Override
  public int length() {
    return cs.length();
  }

  @Override
  public char charAt(int index) {
    return cs.charAt(index);
  }

  @Override
  public CharSequenceView subSequence(int start, int end) {
    return new CharSequenceView(cs.subSequence(start, end));
  }

  @Override
  public String toString() {
    return cs.toString();
  }

  @Override
  public Seq upperCase() {
    return new UpperCase(cs);
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    appendable.append(cs);
  }

  @Override
  public void appendTo(StringBuilder sb) {
    sb.append(cs);
  }

  @Override
  public int indexOf(char c) {
    return Utils.indexOf(cs, c);
  }
}
