package org.fastprintf.seq;

import org.fastprintf.util.Preconditions;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

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
    // let the tree grow to the right, so the deque stack max size could be smaller
    if (left instanceof Concat) {
      Concat leftConcat = (Concat) left;
      return concat0(leftConcat.left, concat0(leftConcat.right, right));
    } else {
      return concat0(left, right);
    }
  }

  private static Concat concat0(Seq left, Seq right) {
    int length = left.length() + right.length();
    return new Concat(left, right, length);
  }

  private static Seq prependHead(Seq currentHead, SimpleSeq seq) {
    if (currentHead != null) {
      return concat(currentHead, seq);
    }
    return seq;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, length);
    for (SimpleSeq seq : this) {
      if (index < seq.length()) {
        return seq.charAt(index);
      }
      index -= seq.length();
    }
    throw new AssertionError();
  }

  @Override
  public Seq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) return Seq.empty();
    if (start == 0 && end == length) return this;
    Seq head = null;
    for (SimpleSeq seq : this) {
      int seqLength = seq.length();
      if (start < seqLength) {
        if (end <= seqLength) {
          head = prependHead(head, seq.subSequence(start, end));
          break;
        }
        head = prependHead(head, seq.subSequence(start, seqLength));
        start = 0;
        end -= seqLength;
      } else {
        start -= seqLength;
        end -= seqLength;
      }
    }
    assert head != null;
    return head;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(length);
    appendTo(sb);
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
    Seq head = null;
    for (SimpleSeq seq : this) {
      head = prependHead(head, seq.upperCase());
    }
    return head;
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    for (SimpleSeq seq : this) {
      seq.appendTo(appendable);
    }
  }

  @Override
  public void appendTo(StringBuilder sb) {
    for (SimpleSeq seq : this) {
      seq.appendTo(sb);
    }
  }

  @Override
  public int indexOf(char c) {
    int currentLength = 0;
    for (SimpleSeq seq : this) {
      int index = seq.indexOf(c);
      if (index != INDEX_NOT_FOUND) {
        return index + currentLength;
      }
      currentLength += seq.length();
    }
    return INDEX_NOT_FOUND;
  }

  @Override
  public Iterator<SimpleSeq> iterator() {
    return new ConcatIterator(this);
  }

  private static final class ConcatIterator implements Iterator<SimpleSeq> {

    private final Deque<Seq> deque = new ArrayDeque<>(8);

    ConcatIterator(Concat concat) {
      deque.push(concat.right);
      deque.push(concat.left);
    }

    @Override
    public boolean hasNext() {
      return !deque.isEmpty();
    }

    @Override
    public SimpleSeq next() {
      Seq seq = deque.pop();
      while (seq instanceof Concat) {
        Concat concat = (Concat) seq;
        deque.push(concat.right);
        deque.push(concat.left);
        seq = deque.pop();
      }
      return (SimpleSeq) seq;
    }
  }
}
