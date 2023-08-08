package org.fastprintf.number;

public interface NumberForm {

  int signum();

  default boolean isNegative() {
    return signum() < 0;
  }

  default boolean isPositive() {
    return signum() > 0;
  }

  default boolean isZero() {
    return signum() == 0;
  }
}
