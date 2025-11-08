package io.fastprintf.seq;

public interface AtomicSeq extends Seq {

  @Override
  AtomicSeq subSequence(int start, int end);

  @Override
  AtomicSeq upperCase();

  @Override
  default int elementCount() {
    return 1;
  }

  default boolean isAtomic() {
    return true;
  }
}
