package org.fastprintf.seq;

import java.io.IOException;

public final class EmptySeq implements Seq {

  static final EmptySeq INSTANCE = new EmptySeq();

  private EmptySeq() {}

  @Override
  public Seq prepend(Seq seq) {
    return seq;
  }

  @Override
  public Seq append(Seq seq) {
    return seq;
  }

  @Override
  public int length() {
    return 0;
  }

  @Override
  public char charAt(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public Seq subSequence(int start, int end) {
    if (start == 0 && end == 0) return this;
    throw new IllegalArgumentException();
  }

  @Override
  public Seq upperCase() {
    return this;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {}

  @Override
  public void appendTo(StringBuilder sb) {}

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int indexOf(char c) {
    return INDEX_NOT_FOUND;
  }
}
