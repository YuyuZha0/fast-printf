package org.fastprintf;

// https://cplusplus.com/reference/cstdio/printf/
public enum Specifier {
  SIGNED_DECIMAL_INTEGER,
  UNSIGNED_DECIMAL_INTEGER,
  UNSIGNED_OCTAL_INTEGER,
  UNSIGNED_HEXADECIMAL_INTEGER,
  UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE,
  DECIMAL_FLOATING_POINT,
  DECIMAL_FLOATING_POINT_UPPERCASE,
  SCIENTIFIC_NOTATION,
  SCIENTIFIC_NOTATION_UPPERCASE,
  USE_SHORTEST_PRESENTATION,
  USE_SHORTEST_PRESENTATION_UPPERCASE,
  HEXADECIMAL_FLOATING_POINT,
  HEXADECIMAL_FLOATING_POINT_UPPERCASE,
  CHARACTER,
  STRING,
  NOTHING_PRINTED,
  PERCENT_SIGN;

  Specifier() {}

  public static Specifier valueOf(char c) {
    switch (c) {
      case 'd':
      case 'i':
        return SIGNED_DECIMAL_INTEGER;
      case 'u':
        return UNSIGNED_DECIMAL_INTEGER;
      case 'o':
        return UNSIGNED_OCTAL_INTEGER;
      case 'x':
        return UNSIGNED_HEXADECIMAL_INTEGER;
      case 'X':
        return UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE;
      case 'f':
        return DECIMAL_FLOATING_POINT;
      case 'F':
        return DECIMAL_FLOATING_POINT_UPPERCASE;
      case 'e':
        return SCIENTIFIC_NOTATION;
      case 'E':
        return SCIENTIFIC_NOTATION_UPPERCASE;
      case 'g':
        return USE_SHORTEST_PRESENTATION;
      case 'G':
        return USE_SHORTEST_PRESENTATION_UPPERCASE;
      case 'a':
        return HEXADECIMAL_FLOATING_POINT;
      case 'A':
        return HEXADECIMAL_FLOATING_POINT_UPPERCASE;
      case 'c':
        return CHARACTER;
      case 's':
        return STRING;
      case 'n':
        return NOTHING_PRINTED;
      case '%':
        return PERCENT_SIGN;
      case 'p':
        throw new UnsupportedOperationException("Pointer not supported");
      default:
        return null;
    }
  }
}
