package io.fastprintf.number;

import static org.junit.Assert.*;

import org.junit.Test;

public class NumberFormTest {

  @Test
  public void testSignum() {
    NumberForm nfPositive = new NumberFormImpl(1);
    assertEquals(1, nfPositive.signum());
    assertTrue(nfPositive.isPositive());
    assertFalse(nfPositive.isNegative());
    assertFalse(nfPositive.isZero());

    NumberForm nfNegative = new NumberFormImpl(-1);
    assertEquals(-1, nfNegative.signum());
    assertFalse(nfNegative.isPositive());
    assertTrue(nfNegative.isNegative());
    assertFalse(nfNegative.isZero());

    NumberForm nfZero = new NumberFormImpl(0);
    assertEquals(0, nfZero.signum());
    assertFalse(nfZero.isPositive());
    assertFalse(nfZero.isNegative());
    assertTrue(nfZero.isZero());
  }

  public static class NumberFormImpl implements NumberForm {
    private final int signum;

    public NumberFormImpl(int signum) {
      this.signum = signum;
    }

    @Override
    public int signum() {
      return signum;
    }
  }
}
