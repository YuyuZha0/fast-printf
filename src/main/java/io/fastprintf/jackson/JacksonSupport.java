package io.fastprintf.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.NullTraits;

/** Keeps optional Jackson linkage out of ordinary argument dispatch. */
public final class JacksonSupport {
  private static final boolean JACKSON_AVAILABLE = isJacksonAvailable();

  private JacksonSupport() {}

  private static boolean isJacksonAvailable() {
    try {
      Class.forName(
          "com.fasterxml.jackson.databind.JsonNode", false, JacksonSupport.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException | LinkageError unavailable) {
      return false;
    }
  }

  public static FormatTraits wrap(Object value) {
    return JACKSON_AVAILABLE ? Adapter.wrap(value) : null;
  }

  private static final class Adapter {
    static FormatTraits wrap(Object value) {
      if (!(value instanceof JsonNode)) {
        return null;
      }
      JsonNode node = (JsonNode) value;
      return node.isNull() ? NullTraits.getInstance() : new JsonNodeTraits(node);
    }
  }
}
