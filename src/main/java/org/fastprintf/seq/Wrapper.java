package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;
import java.util.Objects;

public final class Wrapper implements Seq {

  private final CharSequence cs;

  public Wrapper(CharSequence cs) {
    this.cs = Objects.requireNonNull(cs);
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
  public Wrapper subSequence(int start, int end) {
    return new Wrapper(cs.subSequence(start, end));
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
  public int indexOf(char c) {
    if (cs instanceof String) {
      int index = ((String) cs).indexOf(c);
      return index >= 0 ? index : Utils.INDEX_NOT_FOUND;
    }
    return Utils.indexOf(cs, c);
  }
}
