package io.fastprintf.seq;

public interface SimpleSeq extends Seq {

  @Override
  SimpleSeq subSequence(int start, int end);

  @Override
  SimpleSeq upperCase();
}
