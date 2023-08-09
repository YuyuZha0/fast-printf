package org.fastprintf.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Concat implements Seq {

  private final Seq[] sequences;
  private final int length;

  Concat(List<Seq> sequences) {
    this.sequences = sequences.toArray(new Seq[0]);
    this.length = length(this.sequences);
  }

  private Concat(Seq[] sequences, int length) {
    this.sequences = sequences;
    this.length = length;
  }

  private static int length(Seq[] sequences) {
    int length = 0;
    for (Seq seq : sequences) {
      length += seq.length();
    }
    return length;
  }

  private static Seq[] concat(Seq[] a, Seq[] b) {
    if (a.length == 1 && b.length == 1) {
      return new Seq[] {a[0], b[0]};
    }
    if (a.length == 1) {
      return concat(a[0], b);
    }
    if (b.length == 1) {
      return concat(a, b[0]);
    }
    Seq[] c = new Seq[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  private static Seq[] concat(Seq a0, Seq[] b) {
    Seq[] c = new Seq[b.length + 1];
    c[0] = a0;
    System.arraycopy(b, 0, c, 1, b.length);
    return c;
  }

  private static Seq[] concat(Seq[] a, Seq b0) {
    Seq[] c = new Seq[a.length + 1];
    System.arraycopy(a, 0, c, 0, a.length);
    c[a.length] = b0;
    return c;
  }

  List<Seq> getSequences() {
    return Collections.unmodifiableList(Arrays.asList(sequences));
  }

  @Override
  public int length() {
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
    List<Seq> list = new ArrayList<>(sequences.length);
    for (Seq seq : sequences) {
      int seqLength = seq.length();
      if (start < seqLength) {
        if (end <= seqLength) {
          list.add(seq.subSequence(start, end));
          break;
        }
        list.add(seq.subSequence(start, seqLength));
        start = 0;
        end -= seqLength;
      } else {
        start -= seqLength;
        end -= seqLength;
      }
    }
    return new Concat(list);
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
    if (seq.isEmpty()) return this;
    if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      return new Concat(concat(concat.sequences, sequences), length + concat.length);
    }
    return new Concat(concat(seq, sequences), length + seq.length());
  }

  @Override
  public Seq append(Seq seq) {
    if (seq.isEmpty()) return this;
    if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      return new Concat(concat(sequences, concat.sequences), length + concat.length);
    }
    return new Concat(concat(sequences, seq), length + seq.length());
  }

  @Override
  public Seq dup() {
    return new Concat(sequences.clone(), length);
  }

  @Override
  public Seq upperCase() {
    Seq[] seqs = new Seq[sequences.length];
    for (int i = 0; i < seqs.length; ++i) {
      seqs[i] = sequences[i].upperCase();
    }
    return new Concat(seqs, length);
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    for (Seq seq : sequences) {
      seq.appendTo(appendable);
    }
  }

  @Override
  public void appendTo(StringBuilder sb) {
    for (Seq seq : sequences) {
      seq.appendTo(sb);
    }
  }

  @Override
  public int indexOf(char c) {
    int index = 0;
    for (Seq seq : sequences) {
      int i = seq.indexOf(c);
      if (i >= 0) return index + i;
      index += seq.length();
    }
    return INDEX_NOT_FOUND;
  }
}
