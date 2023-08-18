package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;

import java.io.IOException;
import java.util.function.Function;

public interface Seq extends CharSequence {

  int INDEX_NOT_FOUND = -1;

  static SimpleSeq ch(char c) {
    return Repeated.ofSingleChar(c);
  }

  static SimpleSeq repeated(char c, int count) {
    Preconditions.checkArgument(count >= 1, "count < 1");
    if (count == 1) return ch(c);
    return new Repeated(c, count);
  }

  static SimpleSeq wrap(String s) {
    Preconditions.checkNotNull(s, "s");
    int length = s.length();
    if (length > 0) {
      if (length == 1) return ch(s.charAt(0));
      return new StrView(s, 0, length);
    }
    return empty();
  }

  static SimpleSeq wrap(String s, int start) {
    return wrap(s, start, s.length());
  }

  static SimpleSeq wrap(String s, int start, int end) {
    Preconditions.checkNotNull(s, "s");
    int length = s.length();
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) return empty();
    if (end == start + 1) return ch(s.charAt(start));
    return new StrView(s, start, end - start);
  }

  static SimpleSeq forArray(char[] ch, int start, int length) {
    Preconditions.checkNotNull(ch, "ch");
    Preconditions.checkPositionIndexes(start, start + length, ch.length);
    return new CharArray(ch, start, length, false);
  }

  static SimpleSeq forArray(char[] ch) {
    Preconditions.checkNotNull(ch, "ch");
    return new CharArray(ch, 0, ch.length, false);
  }

  static Seq concat(Seq left, Seq right) {
    Preconditions.checkNotNull(left, "left");
    Preconditions.checkNotNull(right, "right");
    return Concat.concat(left, right);
  }

  static SimpleSeq empty() {
    return EmptySeq.INSTANCE;
  }

  default Seq prepend(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(seq, this);
  }

  default Seq append(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(this, seq);
  }

  @Override
  Seq subSequence(int start, int end);

  default void appendTo(Appendable appendable) throws IOException {
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

  Seq upperCase();

  default Seq map(Function<? super Seq, ? extends Seq> mapper) {
    Preconditions.checkNotNull(mapper, "mapper");
    return mapper.apply(this);
  }

  default boolean isEmpty() {
    return length() == 0;
  }

  default int indexOf(char c) {
    int length = length();
    for (int i = 0; i < length; i++) {
      if (charAt(i) == c) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }
}
