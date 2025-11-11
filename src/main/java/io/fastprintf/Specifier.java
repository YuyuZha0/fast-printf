package io.fastprintf;

// https://cplusplus.com/reference/cstdio/printf/
public enum Specifier {
  SIGNED_DECIMAL_INTEGER('d'),
  UNSIGNED_DECIMAL_INTEGER('u'),
  UNSIGNED_OCTAL_INTEGER('o'),
  UNSIGNED_HEXADECIMAL_INTEGER('x'),
  UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE('X'),
  DECIMAL_FLOATING_POINT('f'),
  DECIMAL_FLOATING_POINT_UPPERCASE('F'),
  SCIENTIFIC_NOTATION('e'),
  SCIENTIFIC_NOTATION_UPPERCASE('E'),
  USE_SHORTEST_PRESENTATION('g'),
  USE_SHORTEST_PRESENTATION_UPPERCASE('G'),
  HEXADECIMAL_FLOATING_POINT('a'),
  HEXADECIMAL_FLOATING_POINT_UPPERCASE('A'),
  CHARACTER('c'),
  STRING('s'),
  STRING_UPPERCASE('S'),
  DATE_AND_TIME('t'),
  DATE_AND_TIME_UPPERCASE('T'),
  NOTHING_PRINTED('n'),
  POINTER('p'),
  PERCENT_SIGN('%');

  private static final Specifier[] VALUES = values();
  private final char c;

  Specifier(char c) {
    this.c = c;
  }

  public static Specifier valueOf(char c) {
    if (c == 'i') {
      return SIGNED_DECIMAL_INTEGER;
    }
    for (Specifier specifier : VALUES) {
      if (specifier.c == c) {
        return specifier;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return Character.toString(c);
  }

  public boolean isDateTimeSpecifier() {
    return this == DATE_AND_TIME || this == DATE_AND_TIME_UPPERCASE;
  }
}
