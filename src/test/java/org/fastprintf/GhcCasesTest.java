package org.fastprintf;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class GhcCasesTest {

  private static final Pattern LL = Pattern.compile("(?:ll|hh?)(?=[diuoxX])");

  private static List<String> readLines() {
    try (InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("printf-tests.txt")) {
      assert inputStream != null;
      try (InputStreamReader inputStreamReader =
          new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
        List<String> list = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
          String line;
          while ((line = bufferedReader.readLine()) != null) {
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("#")) {
              continue;
            }
            list.add(trim);
          }
        }
        return list;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object tryParsePrimitive(String s) {
    if (s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    if (s.startsWith("'") && s.endsWith("'")) {
      return s.charAt(1);
    }
    if (s.startsWith("0x")) {
      return Long.parseLong(s.substring(2), 16);
    }
    if (s.startsWith("0o")) {
      return Long.parseLong(s.substring(2), 8);
    }
    if (s.indexOf('.') > 0) {
      return Double.parseDouble(s);
    }
    if (s.endsWith("U")) {
      return Long.parseUnsignedLong(s.substring(0, s.length() - 1));
    }
    if (s.endsWith("LL")) {
      return Long.parseLong(s.substring(0, s.length() - 2));
    }
    return Integer.parseInt(s);
  }

  private static Object tryParseBigDecimal(String s) {
    if (s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    if (s.startsWith("'") && s.endsWith("'")) {
      return s.charAt(1);
    }
    if (s.startsWith("0x")) {
      return new BigInteger(s.substring(2), 16);
    }
    if (s.startsWith("0o")) {
      return new BigInteger(s.substring(2), 8);
    }
    if (s.indexOf('.') > 0) {
      return new BigDecimal(s);
    }
    if (s.endsWith("U")) {
      return new BigInteger(s.substring(0, s.length() - 1));
    }
    if (s.endsWith("LL")) {
      return new BigInteger(s.substring(0, s.length() - 2));
    }
    return new BigInteger(s);
  }

  private static String fixFormat(String format) {
    format = unwrapQuotes(format);
    if (!format.contains("%")) {
      return format;
    }
    return LL.matcher(format).replaceAll("");
  }

  private static String join(String... args) {
    StringJoiner sj = new StringJoiner(", ");
    Arrays.stream(args).forEach(sj::add);
    return sj.toString();
  }

  private static String unwrapQuotes(String s) {
    return s.substring(1, s.length() - 1);
  }

  private static String[] splitLine(String line) {
    List<String> list = new ArrayList<>();
    int len = line.length();
    int i = 0, start = 0;
    boolean meetSpace = false;
    while (i < len) {
      char c = line.charAt(i);
      if (c == '"') {
        start = i;
        while (line.charAt(++i) != '"') {}
        ++i;
        list.add(line.substring(start, i));
        start = i;
        continue;
      }
      if (c == ' ') {
        if (!meetSpace) {
          list.add(line.substring(start, i));
        }
        meetSpace = true;
      } else {
        if (meetSpace) start = i;
        meetSpace = false;
      }
      i++;
    }
    if (start < len) {
      list.add(line.substring(start));
    }

    return list.toArray(new String[0]);
  }

  @Test
  public void testBigDecimal() {
    for (String line : readLines()) {
      String[] a = splitLine(line);
      if ("!H".equals(a[0]) || "!CH".equals(a[0]) || "?".equals(a[1])) {
        continue;
      }
      String expected = unwrapQuotes(a[1]);
      String format = fixFormat(a[2]);
      // for big integer, unsigned case is complicated, so just ignore it
      if ("xXou".indexOf(format.charAt(format.length() - 1)) >= 0) {
        continue;
      }
      Args args = Args.create();
      for (int i = 3; i < a.length; i++) {
        args.put(tryParseBigDecimal(a[i]));
      }
      FastPrintf fastPrintf = FastPrintf.compile(format);
      String actual = fastPrintf.format(args);
      System.out.println("^" + line + "$");
      assertEquals(join(a), expected, actual);
    }
  }

  @Test
  public void testPrimitive() {
    for (String line : readLines()) {
      String[] a = splitLine(line);
      if ("!H".equals(a[0]) || "!CH".equals(a[0]) || "?".equals(a[1])) {
        continue;
      }
      String expected = unwrapQuotes(a[1]);
      String format = fixFormat(a[2]);
      Args args = Args.create();
      for (int i = 3; i < a.length; i++) {
        args.put(tryParsePrimitive(a[i]));
      }
      FastPrintf fastPrintf = FastPrintf.compile(format);
      String actual = fastPrintf.format(args);
      System.out.println("^" + line + "$");
      assertEquals(join(a), expected, actual);
    }
  }
}
