package io.fastprintf.number;

import org.junit.Test;

public class DoubleWrapperTest {

  @Test
  public void test() {
    DoubleWrapper box = new DoubleWrapper(999);

    System.out.println(box.generalLayout(10));
    System.out.println(box.scientificLayout(10));
    System.out.println(box.decimalLayout(10));
    System.out.println(box.hexLayout(10));
    System.out.println(box);
  }
}
