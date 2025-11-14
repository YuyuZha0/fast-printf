package io.fastprintf.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class TestHelper {

  private static final int JAVA_VERSION = getMajorJavaVersion();

  private TestHelper() {
    throw new IllegalStateException();
  }

  private static int getMajorJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return -1; // Unable to parse version
    }
  }

  public static int currentJavaVersion() {
    return JAVA_VERSION;
  }

  public static <T> void testPrivateConstructor_forCodeCoverage(Class<T> type) throws Exception {
    // This test is purely for achieving 100% code coverage by invoking the private
    // constructor, which is designed to prevent instantiation.
    Constructor<T> constructor = type.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected an exception from the private constructor");
    } catch (InvocationTargetException e) {
      // We expect the constructor to throw an exception.
      // Check that the cause is what we expect (e.g., IllegalStateException).
      Throwable cause = e.getCause();
      assertTrue(
          cause instanceof AssertionError
              || cause instanceof IllegalStateException
              || cause instanceof UnsupportedOperationException);
    }
  }
}
