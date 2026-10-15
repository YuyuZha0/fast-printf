package io.fastprintf.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.fastprintf.FastPrintf;
import java.io.IOException;

/**
 * Writes a value as a JSON string using a compiled formatter supplied by {@link FastPrintfModule}.
 * Jackson handles quoting, escaping and ordinary null properties.
 */
public final class FastPrintfSerializer extends StdSerializer<Object> {
  private final FastPrintf formatter;

  FastPrintfSerializer(FastPrintf formatter) {
    super(Object.class);
    this.formatter = formatter;
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(formatter.format(value));
  }
}
