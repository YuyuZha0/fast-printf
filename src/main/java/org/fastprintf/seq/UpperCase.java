package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;
import java.util.Objects;

public final class UpperCase implements Seq {

  private final CharSequence cs;

  UpperCase(CharSequence cs) {
    this.cs = Objects.requireNonNull(cs);
  }

  @Override
  public int length() {
    return cs.length();
  }

  @Override
  public char charAt(int index) {
    char c = cs.charAt(index);
    return Utils.isLowerCase(c) ? Utils.toUpperCase(c) : c;
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
    // TODO optimize for StringBuilder
    appendable.append(toString());
  }
}
