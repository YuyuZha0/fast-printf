package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.util.function.Consumer;

final class LazySeq implements AtomicSeq {

  private final Consumer<? super StringBuilder> action;
  private final int length;

  LazySeq(Consumer<? super StringBuilder> action, int length) {
    this.action = action;
    this.length = length;
  }

  static StringBuilder buildEagerly(Consumer<? super StringBuilder> action, int length) {
    StringBuilder sb = new StringBuilder(length);
    action.accept(sb);
    if (length != sb.length()) {
      throw new IllegalStateException(
          "Length mismatch: expected " + length + " but got " + sb.length());
    }
    return sb;
  }

  @Override
  public AtomicSeq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    String value = buildEagerly(action, length).subSequence(start, end).toString();
    return Seq.wrap(value);
  }

  @Override
  public AtomicSeq upperCase() {
    return Seq.wrap(toString()).upperCase();
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    return buildEagerly(action, length).charAt(index);
  }

  @Override
  public String toString() {
    return buildEagerly(action, length).toString();
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    appendable.append(toString());
  }

  @Override
  public void appendTo(StringBuilder sb) {
    sb.ensureCapacity(sb.length() + length);
    action.accept(sb);
  }
}
