package org.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CharArrayTest {

  @Test
  public void test() {
    char[] chars = "Hello".toCharArray();
    Seq seq = Seq.forArray(chars);

    assertEquals(5, seq.length());
    assertEquals('H', seq.charAt(0));
    assertEquals('e', seq.charAt(1));
    assertEquals('l', seq.charAt(2));
    assertEquals('l', seq.charAt(3));
    assertEquals('o', seq.charAt(4));

    assertEquals("Hello", seq.toString());
    assertEquals("Hello", seq.subSequence(0, 5).toString());
    assertEquals("ell", seq.subSequence(1, 4).toString());
    assertEquals("H", seq.subSequence(0, 1).toString());

    assertEquals("Hello World", seq.append(Seq.wrap(" World")).toString());
    assertEquals("Hello World", Seq.wrap(" World").prepend(seq).toString());

    assertEquals(0, seq.indexOf('H'));
    assertEquals(1, seq.indexOf('e'));
    assertEquals(2, seq.indexOf('l'));
    assertEquals(4, seq.indexOf('o'));
    assertEquals(-1, seq.indexOf('x'));
  }

  @Test
  public void testUpperCase() {
    String str = "HELLO WORLD 12345";
    Seq seq = Seq.forArray(str.toCharArray());
    assertEquals(str, seq.toString());
    assertEquals(str, seq.upperCase().toString());
    assertSame(seq, seq.upperCase());
  }
}
