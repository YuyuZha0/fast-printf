package org.fastprintf.util;

public final class Utils {

  /** A bit mask which selects the bit encoding ASCII character case. */
  private static final char CASE_MASK = 0x20;

  private Utils() {
    throw new IllegalStateException();
  }

  public static void toUpperCase(char[] chars) {
    int length = chars.length;
    for (int i = 0; i < length; i++) {
      char c = chars[i];
      if (isLowerCase(c)) {
        chars[i] = (char) (c ^ CASE_MASK);
      }
    }
  }

  public static String toUpperCase(String string) {
    int length = string.length();
    for (int i = 0; i < length; i++) {
      if (isLowerCase(string.charAt(i))) {
        char[] chars = string.toCharArray();
        for (; i < length; i++) {
          char c = chars[i];
          if (isLowerCase(c)) {
            chars[i] = (char) (c ^ CASE_MASK);
          }
        }
        return String.valueOf(chars);
      }
    }
    return string;
  }

  /**
   * Indicates whether {@code c} is one of the twenty-six lowercase ASCII alphabetic characters
   * between {@code 'a'} and {@code 'z'} inclusive. All others (including non-ASCII characters)
   * return {@code false}.
   */
  public static boolean isLowerCase(char c) {
    // Note: This was benchmarked against the alternate expression "(char)(c - 'a') < 26" (Nov '13)
    // and found to perform at least as well, or better.
    return (c >= 'a') && (c <= 'z');
  }

  public static char toUpperCase(char c) {
    return (char) (c ^ CASE_MASK);
  }

  public static boolean isDigit(char c) {
    return (c >= '0') && (c <= '9');
  }

  public static boolean isNotDigit(char c) {
    return !isDigit(c);
  }
}
