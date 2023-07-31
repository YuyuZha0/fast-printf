package org.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmptySeqTest {

  @Test
  public void test() {
    Seq seq = EmptySeq.INSTANCE;
    assertEquals(0, seq.length());
    assertEquals("", seq.toString());
    assertEquals("", seq.subSequence(0, 0).toString());

    assertEquals("Hello", seq.append(Seq.wrap("Hello")).toString());
    assertEquals("Hello", Seq.wrap("Hello").prepend(seq).toString());

    assertEquals(-1, seq.indexOf('H'));
  }
}
