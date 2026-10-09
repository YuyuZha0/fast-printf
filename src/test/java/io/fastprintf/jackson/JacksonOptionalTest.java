package io.fastprintf.jackson;

import static org.junit.Assert.*;

import io.fastprintf.FastPrintf;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class JacksonOptionalTest {
  @Test
  public void coreWorksWithoutJackson() throws Exception {
    verifyFallback(false);
  }

  @Test
  public void coreWorksWhenJacksonCannotLink() throws Exception {
    verifyFallback(true);
  }

  private void verifyFallback(boolean brokenInstallation) throws Exception {
    Set<String> requestedClasses = new HashSet<>();
    URL classes = FastPrintf.class.getProtectionDomain().getCodeSource().getLocation();
    try (URLClassLoader isolated =
        new URLClassLoader(new URL[] {classes}, null) {
          @Override
          protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            requestedClasses.add(name);
            if (name.startsWith("com.fasterxml.jackson.")) {
              if (brokenInstallation) throw new NoClassDefFoundError("Jackson core is absent");
              throw new ClassNotFoundException(name);
            }
            return super.loadClass(name, resolve);
          }
        }) {
      Class<?> api = isolated.loadClass("io.fastprintf.FastPrintf");
      Object formatter = api.getMethod("compile", String.class).invoke(null, "%s %d");
      Object value =
          new Object() {
            @Override
            public String toString() {
              return "fallback";
            }
          };
      assertEquals(
          "fallback 42",
          api.getMethod("format", Object[].class)
              .invoke(formatter, new Object[] {new Object[] {value, 42}}));
      assertTrue(requestedClasses.contains("com.fasterxml.jackson.databind.JsonNode"));
      assertFalse(requestedClasses.contains("io.fastprintf.jackson.JacksonSupport$Adapter"));
      assertFalse(requestedClasses.contains("io.fastprintf.jackson.JsonNodeTraits"));
    }
  }
}
