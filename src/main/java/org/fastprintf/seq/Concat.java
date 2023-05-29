package org.fastprintf.seq;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public final class Concat implements Seq {

  private final Deque<Seq> sequences;

  Concat(Deque<Seq> sequences) {
    this.sequences = sequences;
  }

  Deque<Seq> getSequences() {
    return sequences;
  }

  @Override
  public int length() {
    int length = 0;
    for (Seq seq : sequences) {
      length += seq.length();
    }
    return length;
  }

  @Override
  public char charAt(int index) {
    for (Seq seq : sequences) {
      int length = seq.length();
      if (index < length) {
        return seq.charAt(index);
      }
      index -= length;
    }
    throw new IndexOutOfBoundsException();
  }

  @Override
  public Seq subSequence(int start, int end) {
    int length = length();
    if (start < 0 || end > length || start > end) throw new IllegalArgumentException();
    if (start == end) return Seq.empty();
    Deque<Seq> deque = new ArrayDeque<>(sequences.size());
    for (Seq seq : sequences) {
      int seqLength = seq.length();
      if (start < seqLength) {
        if (end <= seqLength) {
          deque.addLast(seq.subSequence(start, end));
          break;
        }
        deque.addLast(seq.subSequence(start, seqLength));
        start = 0;
        end -= seqLength;
      } else {
        start -= seqLength;
        end -= seqLength;
      }
    }
    return new Concat(deque);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(length());
    for (Seq seq : sequences) {
      sb.append(seq);
    }
    return sb.toString();
  }

  @Override
  public Seq prepend(Seq seq) {
    if (seq.length() == 0) return this;
    if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      concat.sequences.addAll(sequences);
      return seq;
    }
    sequences.addFirst(seq);
    return this;
  }

  @Override
  public Seq append(Seq seq) {
    if (seq.length() == 0) return this;
    if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      sequences.addAll(concat.sequences);
      return this;
    }
    sequences.addLast(seq);
    return this;
  }

  @Override
  public Seq dup() {
    Deque<Seq> deque = new ArrayDeque<>(sequences);
    return new Concat(deque);
  }

  @Override
  public Seq upperCase() {
    Deque<Seq> deque = new ArrayDeque<>(sequences.size());
    for (Seq seq : sequences) {
      deque.addLast(seq.upperCase());
    }
    return new Concat(deque);
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    for (Seq seq : sequences) {
      seq.appendTo(appendable);
    }
  }
}
