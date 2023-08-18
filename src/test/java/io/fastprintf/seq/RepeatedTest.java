package io.fastprintf.seq;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RepeatedTest {

  @Test
  public void test() {
    Seq seq = Seq.repeated('a', 5);
    assertEquals(5, seq.length());
    assertEquals('a', seq.charAt(0));
    assertEquals('a', seq.charAt(1));
    assertEquals('a', seq.charAt(2));
    assertEquals('a', seq.charAt(3));
    assertEquals('a', seq.charAt(4));

    assertEquals("aaaaa", seq.toString());
    assertEquals("aaaaa", seq.subSequence(0, 5).toString());
    assertEquals("aaa", seq.subSequence(0, 3).toString());

    assertEquals("aaaaa World", seq.append(Seq.wrap(" World")).toString());
    assertEquals("aaaaa World", Seq.wrap(" World").prepend(seq).toString());

    assertEquals(0, seq.indexOf('a'));
    assertEquals(-1, seq.indexOf('x'));

    assertEquals("AAAAA", seq.upperCase().toString());
  }

  @Test
  public void testSingleChar() {
    Seq a1 = Seq.ch('a');
    Seq a2 = Seq.wrap("a");

    assertEquals("a", a1.toString());
    assertEquals("a", a2.toString());
    assertEquals("A", a1.upperCase().toString());
    assertEquals("A", a2.upperCase().toString());

    assertSame(a1, a2);
    assertSame(a1.upperCase(), a2.upperCase());
  }
}
