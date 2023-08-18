package io.fastprintf.traits;

import io.fastprintf.number.IntForm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShortTraitsTest {

  @Test
  public void test() {
    verifyShort((short) 0);
    verifyShort((short) 1);
    verifyShort((short) -1);
    verifyShort((short) (Short.MAX_VALUE >>> 1));
    verifyShort(Short.MAX_VALUE);
    verifyShort(Short.MIN_VALUE);
  }

  private void verifyShort(short s) {
    ShortTraits shortTraits = new ShortTraits(s);
    assertEquals(Short.toString(s), shortTraits.asString());
    assertEquals(s, shortTraits.asInt());
    assertEquals((char) s, shortTraits.asChar());
    IntForm intForm = shortTraits.asIntForm();
    assertEquals(String.format("%d", Math.abs(s)), intForm.toDecimalString());
    assertEquals(String.format("%x", s), intForm.toHexString());
    assertEquals(String.format("%o", s), intForm.toOctalString());
    assertEquals(Short.toUnsignedInt(s) + "", intForm.toUnsignedDecimalString());
  }
}
