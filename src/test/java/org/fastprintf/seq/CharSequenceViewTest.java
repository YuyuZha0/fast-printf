package org.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CharSequenceViewTest {

  @Test
  public void test() {
    Seq seq = Seq.wrap((CharSequence) "Hello");

    assertEquals(5, seq.length());
    assertEquals('H', seq.charAt(0));
    assertEquals('e', seq.charAt(1));
    assertEquals('l', seq.charAt(2));
    assertEquals('l', seq.charAt(3));
    assertEquals('o', seq.charAt(4));

    assertEquals("Hello", seq.toString());
    assertEquals("Hello", seq.subSequence(0, 5).toString());
    assertEquals("ell", seq.subSequence(1, 4).toString());

    assertEquals("Hello World", seq.append(Seq.wrap((CharSequence) " World")).toString());
    assertEquals("Hello World", Seq.wrap((CharSequence) " World").prepend(seq).toString());

    assertEquals(0, seq.indexOf('H'));
    assertEquals(1, seq.indexOf('e'));
    assertEquals(2, seq.indexOf('l'));
    assertEquals(4, seq.indexOf('o'));
    assertEquals(-1, seq.indexOf('x'));
  }
}