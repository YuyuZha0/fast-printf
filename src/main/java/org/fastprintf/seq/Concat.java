package org.fastprintf.seq;

import org.fastprintf.util.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public final class Concat implements Seq, Iterable<SimpleSeq> {

  private final Seq left;
  private final Seq right;
  private final int length;

  private Concat(Seq left, Seq right, int length) {
    this.left = left;
    this.right = right;
    this.length = length;
  }

  static Concat concat(Seq left, Seq right) {
    return new Concat(left, right, left.length() + right.length());
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, length);
    int leftLength = left.length();
    if (index < leftLength) {
      return left.charAt(index);
    } else {
      return right.charAt(index - leftLength);
    }
  }

  @Override
  public Seq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) return Seq.empty();
    int leftLength = left.length();
    if (end <= leftLength) {
      return left.subSequence(start, end);
    } else if (start >= leftLength) {
      return right.subSequence(start - leftLength, end - leftLength);
    } else {
      return concat(left.subSequence(start, leftLength), right.subSequence(0, end - leftLength));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(length);
    visit(sb::append);
    return sb.toString();
  }

  @Override
  public Seq prepend(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(seq, this);
  }

  @Override
  public Seq append(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(this, seq);
  }

  @Override
  public Seq upperCase() {
    return new Concat(left.upperCase(), right.upperCase(), length);
  }

  private void visit(Consumer<? super SimpleSeq> consumer) {
    if (left instanceof Concat) {
      ((Concat) left).visit(consumer);
    } else {
      assert left instanceof SimpleSeq;
      consumer.accept((SimpleSeq) left);
    }
    if (right instanceof Concat) {
      ((Concat) right).visit(consumer);
    } else {
      assert right instanceof SimpleSeq;
      consumer.accept((SimpleSeq) right);
    }
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    visit(
        seq -> {
          try {
            seq.appendTo(appendable);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Override
  public void appendTo(StringBuilder sb) {
    visit(seq -> seq.appendTo(sb));
  }

  @Override
  public int indexOf(char c) {
    int leftLength = left.length();
    int index = left.indexOf(c);
    if (index != INDEX_NOT_FOUND) return index;
    index = right.indexOf(c);
    if (index != INDEX_NOT_FOUND) return index + leftLength;
    return INDEX_NOT_FOUND;
  }

  @Override
  public Iterator<SimpleSeq> iterator() {
    List<SimpleSeq> collect = new ArrayList<>();
    visit(collect::add);
    return collect.iterator();
  }
}
