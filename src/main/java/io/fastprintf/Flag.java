package io.fastprintf;

// https://learn.microsoft.com/en-us/cpp/c-runtime-library/format-specification-syntax-printf-and-wprintf-functions?view=msvc-170
public enum Flag {

  // Left align the result within the given field width.
  LEFT_JUSTIFY,

  // When it's used with the o, x, or X format, the # flag uses 0, 0x, or 0X, respectively, to
  // prefix any nonzero output value.
  // When it's used with the e, E, f, F, a, or A format, the # flag forces the output value to
  // contain a decimal point.
  // When it's used with the g or G format, the # flag forces the output value to contain a decimal
  // point and prevents the truncation of trailing zeros.
  ALTERNATE,
  // Use a sign (+ or -) to prefix the output value if it's of a signed type.
  PLUS,
  // Use a blank to prefix the output value if it's signed and positive. The blank is ignored if
  // both the blank and + flags appear.
  LEADING_SPACE,

  // If width is prefixed by 0, leading zeros are added until the minimum width is reached. If both
  // 0
  // and - appear, the 0 is ignored. If 0 is specified for an integer format (i, u, x, X, o, d) and
  // a precision specification is also present—for example, %04.d—the 0 is ignored. If 0 is
  // specified for the a or A floating-point format, leading zeros are prepended to the mantissa,
  // after the 0x or 0X prefix.
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
