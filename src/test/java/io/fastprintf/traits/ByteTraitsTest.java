package io.fastprintf.traits;

import io.fastprintf.number.IntForm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteTraitsTest {

  @Test
  public void test() {
    for (int i = -127; i < 128; ++i) {
      verifyByte((byte) i);
    }
  }

  private void verifyByte(byte b) {
    ByteTraits byteTraits = new ByteTraits(b);
    assertEquals(Byte.toString(b), byteTraits.asString());
    assertEquals(b, byteTraits.asInt());
    assertEquals((char) b, byteTraits.asChar());
    IntForm intForm = byteTraits.asIntForm();
    assertEquals(String.format("%d", Math.abs(b)), intForm.toDecimalString());
    assertEquals(String.format("%x", b), intForm.toHexString());
    assertEquals(String.format("%o", b), intForm.toOctalString());
    assertEquals(Byte.toUnsignedInt(b) + "", intForm.toUnsignedDecimalString());
  }
}
