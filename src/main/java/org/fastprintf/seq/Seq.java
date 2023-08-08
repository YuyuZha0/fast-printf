package org.fastprintf.seq;

import org.fastprintf.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface Seq extends CharSequence {

  static Seq ch(char c) {
    return new Repeated(c, 1);
  }

  static Seq repeated(char c, int count) {
    return new Repeated(c, count);
  }

  static Seq upperCase(CharSequence cs) {
    if (cs instanceof Seq) {
      return upperCase((Seq) cs);
    }
    return new UpperCase(cs);
  }

  static Seq upperCase(Seq seq) {
    return seq.upperCase();
  }

  static Seq wrap(CharSequence cs) {
    if (cs instanceof Seq) return (Seq) cs;
    if (cs.length() == 0) return empty();
    return new Wrapper(cs);
  }

  static Seq wrap(String s) {
    if (!s.isEmpty()) {
      return new Wrapper(s);
    }
    return empty();
  }

  static Seq forArray(char[] ch, int start, int length) {
    return new CharArray(ch, start, length, false);
  }

  static Seq forArray(char[] ch) {
    return new CharArray(ch, 0, ch.length, false);
  }

  static Seq concat(Seq... seqs) {
    if (seqs.length == 0) return empty();
    if (seqs.length == 1) return seqs[0];
    List<Seq> sequences = new ArrayList<>(seqs.length);
    for (Seq seq : seqs) {
      if (seq instanceof Concat) {
        Concat concat = (Concat) seq;
        sequences.addAll(concat.getSequences());
      } else {
        sequences.add(seq);
      }
    }
    return new Concat(sequences);
  }

  static Seq empty() {
    return EmptySeq.INSTANCE;
  }

  default Seq prepend(Seq seq) {
    if (seq.isEmpty()) return this;
    if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      return concat.append(this);
    }
    return concat(seq, this);
  }

  default Seq append(Seq seq) {
    if (seq.isEmpty()) return this;
    if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      return concat.prepend(this);
    }
    return concat(this, seq);
  }

  @Override
  Seq subSequence(int start, int end);

  default void appendTo(Appendable appendable) throws IOException {
    if (appendable instanceof StringBuilder) {
      StringBuilder sb = (StringBuilder) appendable;
      appendTo(sb);
      return;
    }
    int length = length();
    if (length == 0) return;
    for (int i = 0; i < length; i++) {
      appendable.append(charAt(i));
    }
  }

  default void appendTo(StringBuilder sb) {
    int length = length();
    if (length == 0) return;
    for (int i = 0; i < length; i++) {
      sb.append(charAt(i));
    }
  }

  default Seq dup() {
    return this;
  }

  default Seq upperCase() {
    return new UpperCase(this);
  }

  default Seq map(Function<? super Seq, ? extends Seq> mapper) {
    Objects.requireNonNull(mapper, "map");
    return mapper.apply(this);
  }

  default boolean isEmpty() {
    return length() == 0;
  }

  default int indexOf(char c) {
    return Utils.indexOf(this, c);
  }
}
