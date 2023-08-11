package org.fastprintf.seq;

import org.fastprintf.util.Preconditions;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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

  private static void push(Deque<Seq> deque, Seq seq) {
    Concat concat = (Concat) seq;
    deque.push(concat.right);
    deque.push(concat.left);
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
    if (start == 0 && end == length) return this;
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
    Deque<Seq> deque = new ArrayDeque<>();
    push(deque, this);
    Seq head = null;
    while (!deque.isEmpty()) {
      Seq seq = deque.pop();
      if (seq instanceof Concat) {
        push(deque, seq);
      } else {
        if (head != null) {
          head = concat(head, seq.upperCase());
        } else {
          head = seq.upperCase();
        }
      }
    }
    return head;
  }

  private void visit(Consumer<? super SimpleSeq> consumer) {
    // use Deque to avoid recursion/stack overflow
    Deque<Seq> deque = new ArrayDeque<>();
    push(deque, this);
    while (!deque.isEmpty()) {
      Seq seq = deque.pop();
      if (seq instanceof Concat) {
        push(deque, seq);
      } else {
        assert seq instanceof SimpleSeq;
        consumer.accept((SimpleSeq) seq);
      }
    }
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    Deque<Seq> deque = new ArrayDeque<>();
    push(deque, this);
    while (!deque.isEmpty()) {
      Seq seq = deque.pop();
      if (seq instanceof Concat) {
        push(deque, seq);
      } else {
        assert seq instanceof SimpleSeq;
        seq.appendTo(appendable);
      }
    }
  }

  @Override
  public void appendTo(StringBuilder sb) {
    visit(seq -> seq.appendTo(sb));
  }

  @Override
  public int indexOf(char c) {
    Deque<Seq> deque = new ArrayDeque<>();
    push(deque, this);
    int visitedLength = 0;
    while (!deque.isEmpty()) {
      Seq seq = deque.pop();
      if (seq instanceof Concat) {
        push(deque, seq);
      } else {
        int index = seq.indexOf(c);
        if (index != INDEX_NOT_FOUND) {
          return visitedLength + index;
        }
        visitedLength += seq.length();
      }
    }
    return INDEX_NOT_FOUND;
  }

  @Override
  public Iterator<SimpleSeq> iterator() {
    List<SimpleSeq> collect = new ArrayList<>();
    visit(collect::add);
    return collect.iterator();
  }
}
