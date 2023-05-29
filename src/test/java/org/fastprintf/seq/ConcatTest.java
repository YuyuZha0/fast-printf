package org.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcatTest {

  @Test
  public void test() {
    Seq seq = Seq.concat(Seq.wrap("Hello"), Seq.wrap(" World"));

    assertEquals(11, seq.length());
    assertEquals('H', seq.charAt(0));
    assertEquals('e', seq.charAt(1));
    assertEquals('l', seq.charAt(2));
    assertEquals('l', seq.charAt(3));
    assertEquals('o', seq.charAt(4));
    assertEquals(' ', seq.charAt(5));
    assertEquals('W', seq.charAt(6));
    assertEquals('o', seq.charAt(7));
    assertEquals('r', seq.charAt(8));
    assertEquals('l', seq.charAt(9));
    assertEquals('d', seq.charAt(10));

    assertEquals("Hello World", seq.toString());
    assertEquals("Hello World", seq.subSequence(0, 11).toString());
    assertEquals("ello Worl", seq.subSequence(1, 10).toString());
    assertEquals("llo Wor", seq.subSequence(2, 9).toString());
    assertEquals("Hello", seq.subSequence(0, 5).toString());
    assertEquals("World", seq.subSequence(6, 11).toString());

    assertEquals("Hello World!", seq.append(Seq.wrap("!")).toString());
    assertEquals("Hello World!!", Seq.wrap("!").prepend(seq).toString());
  }
}
