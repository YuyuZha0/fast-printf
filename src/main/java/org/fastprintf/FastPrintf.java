package org.fastprintf;

// https://cplusplus.com/reference/cstdio/printf/
public interface FastPrintf {

  static FastPrintf compile(String format) {
    return FastPrintfImpl.compile(format);
  }

  <T extends Appendable> T format(T builder, Args args);

  default String format(Args args) {
    return format(new StringBuilder(), args).toString();
  }
}
