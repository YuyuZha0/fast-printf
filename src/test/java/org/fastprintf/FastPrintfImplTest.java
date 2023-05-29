package org.fastprintf;

import org.junit.Test;

import java.math.BigInteger;
import java.text.NumberFormat;

public class FastPrintfImplTest {

  @Test
  public void test() {
    NumberFormat.getNumberInstance().format(1234567890);
    System.out.println(Integer.toUnsignedString(-9876));
    System.out.println(Double.toHexString(123.456));
    System.out.printf("%o ", Integer.MAX_VALUE);
  }

  @Test
  public void test1() {
    System.out.printf("%x%n", new BigInteger("-12345678901234567890"));
  }
}
