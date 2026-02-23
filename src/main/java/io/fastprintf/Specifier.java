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

  // O(1) lookup table indexed by ASCII character value.
  // Covers the full ASCII printable range (' ' to '~', i.e., 32..126).
  private static final int LOOKUP_OFFSET = ' '; // 32
  private static final Specifier[] LOOKUP;

  static {
    int size = '~' - LOOKUP_OFFSET + 1; // 95 slots
    LOOKUP = new Specifier[size];
    for (Specifier specifier : values()) {
      int idx = specifier.c - LOOKUP_OFFSET;
      if (idx < 0 || idx >= size) {
        throw new ExceptionInInitializerError(
            "Specifier character '"
                + specifier.c
                + "' (value "
                + (int) specifier.c
                + ") is outside the supported ASCII range [' '..'~']");
      }
      LOOKUP[idx] = specifier;
    }
    // 'i' is an alias for signed decimal integer in glibc printf.
    LOOKUP['i' - LOOKUP_OFFSET] = SIGNED_DECIMAL_INTEGER;
  }

  private final char c;

  Specifier(char c) {
    this.c = c;
  }

  public static Specifier valueOf(char c) {
    int idx = c - LOOKUP_OFFSET;
    if (idx >= 0 && idx < LOOKUP.length) {
      return LOOKUP[idx];
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
