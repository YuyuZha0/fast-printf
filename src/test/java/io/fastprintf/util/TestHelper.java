package io.fastprintf.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TestHelper {

  private TestHelper() {
    throw new IllegalStateException();
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
