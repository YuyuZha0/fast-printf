package io.fastprintf.jackson;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fastprintf.FastPrintf;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;

/** Executable counterparts of the README's application-side Jackson examples. */
public class JacksonIntegrationTest {
  public static class Price {
    @JsonSerialize(using = AmountSerializer.class)
    public BigDecimal amount = new BigDecimal("12.3");
  }

  public static class AmountSerializer extends JsonSerializer<BigDecimal> {
    private static final FastPrintf FORMAT = FastPrintf.compile("%.2f");

    @Override
    public void serialize(BigDecimal value, JsonGenerator out, SerializerProvider provider)
        throws IOException {
      out.writeString(FORMAT.format(value));
    }
  }

  @Test
  public void formatsAnnotatedPropertyAsJsonString() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Price price = new Price();
    assertEquals("{\"amount\":\"12.30\"}", mapper.writeValueAsString(price));
    price.amount = null;
    assertEquals("{\"amount\":null}", mapper.writeValueAsString(price));
  }

  @Test
  public void formatsTreeFieldsWithoutManualUnwrapping() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    com.fasterxml.jackson.databind.JsonNode order =
        mapper.readTree("{\"name\":\"Alice\",\"amount\":12.3}");
    assertEquals(
        "Alice: 12.30",
        FastPrintf.compile("%s: %.2f").format(order.get("name"), order.get("amount")));
  }
}
