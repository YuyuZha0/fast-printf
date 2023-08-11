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
    assertEquals("Hello World!", Seq.wrap("!").prepend(seq).toString());

    assertEquals(0, seq.indexOf('H'));
    assertEquals(1, seq.indexOf('e'));
    assertEquals(2, seq.indexOf('l'));
    assertEquals(4, seq.indexOf('o'));
    assertEquals(5, seq.indexOf(' '));
    assertEquals(6, seq.indexOf('W'));
    assertEquals(8, seq.indexOf('r'));
    assertEquals(10, seq.indexOf('d'));
    assertEquals(-1, seq.indexOf('x'));

    assertEquals("HELLO WORLD", seq.upperCase().toString());

    Seq seq1 = Seq.concat(Seq.wrap("123 "), seq);
    assertEquals(15, seq1.length());
    assertEquals('1', seq1.charAt(0));
    assertEquals('2', seq1.charAt(1));
    assertEquals('3', seq1.charAt(2));
    assertEquals(' ', seq1.charAt(3));
    assertEquals('H', seq1.charAt(4));

    assertEquals("123 Hello World", seq1.toString());
    assertEquals("123 Hello World", seq1.subSequence(0, 15).toString());
    assertEquals("23 Hello Worl", seq1.subSequence(1, 14).toString());

    assertEquals("123 HELLO WORLD", seq1.upperCase().toString());

    Seq seq2 = Seq.concat(seq, Seq.wrap(" 123"));
    assertEquals(15, seq2.length());
    assertEquals('H', seq2.charAt(0));
    assertEquals('e', seq2.charAt(1));
    assertEquals('l', seq2.charAt(2));
    assertEquals('l', seq2.charAt(3));
    assertEquals('o', seq2.charAt(4));
    assertEquals(' ', seq2.charAt(5));

    assertEquals("Hello World 123", seq2.toString());
    assertEquals("Hello World 123", seq2.subSequence(0, 15).toString());
    assertEquals("ello World 12", seq2.subSequence(1, 14).toString());

    assertEquals("HELLO WORLD 123", seq2.upperCase().toString());
  }
}
