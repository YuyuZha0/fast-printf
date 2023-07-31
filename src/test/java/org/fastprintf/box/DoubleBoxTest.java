package org.fastprintf.box;

import org.junit.Test;

public class DoubleBoxTest {

  @Test
  public void test() {
    DoubleBox box = new DoubleBox(999);

    System.out.println(box.generalLayout(10));
    System.out.println(box.scientificLayout(10));
    System.out.println(box.decimalLayout(10));
    System.out.println(box.hexLayout(10));
  }
}
