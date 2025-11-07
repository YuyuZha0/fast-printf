package io.fastprintf.seq;

public interface AtomicSeq extends Seq {

  @Override
  AtomicSeq subSequence(int start, int end);

  @Override
  AtomicSeq upperCase();
}
