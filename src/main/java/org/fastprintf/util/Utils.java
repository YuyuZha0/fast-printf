package org.fastprintf.util;

import java.lang.reflect.Array;
import java.util.Iterator;

public final class Utils {

  /** A bit mask which selects the bit encoding ASCII character case. */
  private static final char CASE_MASK = 0x20;

  private Utils() {
    throw new IllegalStateException();
  }

  public static boolean toUpperCase(char[] chars) {
    int length = chars.length;
    boolean mod = false;
    for (int i = 0; i < length; i++) {
      char c = chars[i];
      if (isLowerCase(c)) {
        chars[i] = (char) (c ^ CASE_MASK);
        mod = true;
      }
    }
    return mod;
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
   *
   * @param c the input character
   * @return whether c is in lower case
   */
  public static boolean isLowerCase(char c) {
    // Note: This was benchmarked against the alternate expression "(char)(c - 'a') < 26" (Nov '13)
    // and found to perform at least as well, or better.
    return (c >= 'a') && (c <= 'z');
  }

  public static char toUpperCase(char c) {
    if (isLowerCase(c)) {
      return (char) (c ^ CASE_MASK);
    }
    return c;
  }

  public static boolean isDigit(char c) {
    return (c >= '0') && (c <= '9');
  }

  public static boolean isNotDigit(char c) {
    return !isDigit(c);
  }

  public static StringBuilder join(StringBuilder sb, String separator, Object args) {
    if (args == null) return sb;
    if (args instanceof Iterable) {
      return joinIterator(sb, separator, ((Iterable<?>) args).iterator());
    }
    if (args instanceof Iterator) {
      return joinIterator(sb, separator, (Iterator<?>) args);
    }
    if (args.getClass().isArray()) {
      return joinArray(sb, separator, args);
    }
    return sb.append(args);
  }

  private static StringBuilder joinArray(StringBuilder sb, String separator, Object args) {
    int length = Array.getLength(args);
    if (length == 0) return sb;
    sb.append(Array.get(args, 0));
    for (int i = 1; i < length; i++) {
      sb.append(separator).append(Array.get(args, i));
    }
    return sb;
  }

  private static StringBuilder joinIterator(StringBuilder sb, String separator, Iterator<?> it) {
    if (!it.hasNext()) return sb;
    sb.append(it.next());
    while (it.hasNext()) {
      sb.append(separator).append(it.next());
    }
    return sb;
  }

  public static String join(String separator, Object args) {
    return join(new StringBuilder(), separator, args).toString();
  }
}
