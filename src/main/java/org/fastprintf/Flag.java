package org.fastprintf;

import java.util.UnknownFormatFlagsException;

public enum Flag {
  LEFT_JUSTIFY,
  ALTERNATE,
  PLUS,
  LEADING_SPACE,
  ZERO_PAD;

  public static Flag valueOf(char c) {
    switch (c) {
      case '-':
        return LEFT_JUSTIFY;
      case '#':
        return ALTERNATE;
      case '+':
        return PLUS;
      case ' ':
        return LEADING_SPACE;
      case '0':
        return ZERO_PAD;
      default:
        return null;
    }
  }
}
