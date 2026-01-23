package io.fastprintf.util;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.Iterator;

public final class Utils {

  /** A bit mask which selects the bit encoding ASCII character case. */
  private static final char CASE_MASK = 0x20;

  private static final long MAX_UNSIGNED_INT = 0xFFFFFFFFL;

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

  /**
   * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
   * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
   * return {@code false}.
   *
   * @param c the input character
   * @return whether c is in upper case
   */
  public static boolean isUpperCase(char c) {
    return (c >= 'A') && (c <= 'Z');
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
    sb.append(lenientToString(Array.get(args, 0)));
    for (int i = 1; i < length; i++) {
      sb.append(separator).append(lenientToString(Array.get(args, i)));
    }
    return sb;
  }

  private static StringBuilder joinIterator(StringBuilder sb, String separator, Iterator<?> it) {
    if (!it.hasNext()) return sb;
    sb.append(it.next());
    while (it.hasNext()) {
      sb.append(separator).append(lenientToString(it.next()));
    }
    return sb;
  }

  public static String join(String separator, Object args) {
    return join(new StringBuilder(), separator, args).toString();
  }

  public static Instant longToInstant(long value) {
    Preconditions.checkLongArgument(
        value >= 0, "The value must not be negative, but was: %s", value);
    if (value <= MAX_UNSIGNED_INT) return Instant.ofEpochSecond(value);
    return Instant.ofEpochMilli(value);
  }

  private static String lenientToString(Object o) {
    if (o == null) {
      return "null";
    }
    try {
      return o.toString();
    } catch (Exception e) {
      // Default toString() behavior - see Object.toString()
      String objectToString =
          o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o));
      return "<" + objectToString + " threw " + e.getClass().getName() + ">";
    }
  }

  /**
   * Returns the given {@code template} string with each occurrence of {@code "%s"} replaced with
   * the corresponding argument value from {@code args}; or, if the placeholder and argument counts
   * do not match, returns a best-effort form of that string. Will not throw an exception under
   * normal conditions.
   *
   * <p><b>Note:</b> For most string-formatting needs, use {@link String#format String.format},
   * {@link java.io.PrintWriter#format PrintWriter.format}, and related methods. These support the
   * full range of <a
   * href="https://docs.oracle.com/javase/9/docs/api/java/util/Formatter.html#syntax">format
   * specifiers</a>, and alert you to usage errors by throwing {@link
   * java.util.IllegalFormatException}.
   *
   * <p>In certain cases, such as outputting debugging information or constructing a message to be
   * used for another unchecked exception, an exception during string formatting would serve little
   * purpose except to supplant the real information you were trying to provide. These are the cases
   * this method is made for; it instead generates a best-effort string with all supplied argument
   * values present. This method is also useful in environments such as GWT where {@code
   * String.format} is not available. As an example, method implementations of the {@link
   * Preconditions} class use this formatter, for both of the reasons just discussed.
   *
   * <p><b>Warning:</b> Only the exact two-character placeholder sequence {@code "%s"} is
   * recognized.
   *
   * @param template a string containing zero or more {@code "%s"} placeholder sequences. {@code
   *     null} is treated as the four-character string {@code "null"}.
   * @param args the arguments to be substituted into the message template. The first argument
   *     specified is substituted for the first occurrence of {@code "%s"} in the template, and so
   *     forth. A {@code null} argument is converted to the four-character string {@code "null"};
   *     non-null values are converted to strings using {@link Object#toString()}.
   * @since 25.1
   * @return the formatted string
   */
  public static String lenientFormat(String template, Object... args) {
    if (template == null) {
      return "null";
    }
    if (args == null) {
      args = new Object[] {"(Object[])null"};
    } else {
      for (int i = 0; i < args.length; i++) {
        args[i] = lenientToString(args[i]);
      }
    }

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template, templateStart, placeholderStart);
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template, templateStart, template.length());

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }

  /**
   * Returns the string representation size for a given long value.
   *
   * @param x long value
   * @return string size
   * @implNote There are other ways to compute this: e.g. binary search, but values are biased
   *     heavily towards zero, and therefore linear search wins. The iteration results are also
   *     routinely inlined in the generated code after loop unrolling.
   */
  public static int stringSize(long x) {
    int d = 1;
    if (x >= 0) {
      d = 0;
      x = -x;
    }
    long p = -10;
    for (int i = 1; i < 19; i++) {
      if (x > p) return i + d;
      p = 10 * p;
    }
    return 19 + d;
  }

  /**
   * Returns the string representation size for a given int value.
   *
   * @param x int value
   * @return string size
   * @implNote There are other ways to compute this: e.g. binary search, but values are biased
   *     heavily towards zero, and therefore linear search wins. The iteration results are also
   *     routinely inlined in the generated code after loop unrolling.
   */
  public static int stringSize(int x) {
    int d = 1;
    if (x >= 0) {
      d = 0;
      x = -x;
    }
    int p = -10;
    for (int i = 1; i < 10; i++) {
      if (x > p) return i + d;
      p = 10 * p;
    }
    return 10 + d;
  }
}
