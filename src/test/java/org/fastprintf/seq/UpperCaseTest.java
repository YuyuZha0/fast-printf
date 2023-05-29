package org.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UpperCaseTest {

  @Test
  public void test() {
    Seq seq = Seq.upperCase("Hello");

    assertEquals(5, seq.length());
    assertEquals('H', seq.charAt(0));
    assertEquals('E', seq.charAt(1));
    assertEquals('L', seq.charAt(2));
    assertEquals('L', seq.charAt(3));
    assertEquals('O', seq.charAt(4));
    assertEquals("HELLO", seq.toString());

    assertEquals("HELLO", seq.subSequence(0, 5).toString());
    assertEquals("ELL", seq.subSequence(1, 4).toString());

    assertEquals("HELLO World", seq.append(Seq.wrap(" World")).toString());
    assertEquals("HELLO World", Seq.wrap(" World").prepend(seq).toString());
  }
}
