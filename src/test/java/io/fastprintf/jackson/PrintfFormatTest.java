package io.fastprintf.jackson;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fastprintf.FastPrintf;
import io.fastprintf.PrintfSyntaxException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;

public class PrintfFormatTest {
  public static class NumericTextValue {
    @PrintfFormat("%.2f")
    public JsonNode value = com.fasterxml.jackson.databind.node.TextNode.valueOf("12.3");
  }

  @Test
  public void formatsNumericTextNodeProperty() throws Exception {
    ObjectMapper mapper = new ObjectMapper().registerModule(new FastPrintfModule());
    assertEquals("{\"value\":\"12.30\"}", mapper.writeValueAsString(new NumericTextValue()));
  }

  public static class ContainerValue {
    @PrintfFormat("%s")
    public Object value;
  }

  @Test
  public void formatsContainersAsOnePropertyArgument() throws Exception {
    ObjectMapper mapper = new ObjectMapper().registerModule(new FastPrintfModule());
    Object[] values = {
      new String[] {"a", "b"},
      java.util.Arrays.asList("a", "b"),
      new int[] {1, 2},
      mapper.readTree("[1,2]"),
      mapper.readTree("42")
    };
    String[] expected = {"[a, b]", "[a, b]", "[1, 2]", "[1,2]", "42"};
    ContainerValue property = new ContainerValue();
    for (int i = 0; i < values.length; i++) {
      property.value = values[i];
      assertEquals(
          expected[i],
          mapper.readTree(mapper.writeValueAsString(property)).get("value").textValue());
    }
  }

  public static class ExplicitSerializer {
    @PrintfFormat("%.2f")
    @JsonSerialize(using = FastPrintfSerializer.class)
    public BigDecimal value = new BigDecimal("12.3");
  }

  public static class DefaultJsonSerialize {
    @PrintfFormat("%.2f")
    @JsonSerialize
    public BigDecimal value = new BigDecimal("12.3");
  }

  @Test
  public void requiresModuleAndExplicitPattern() throws Exception {
    ObjectMapper plain = new ObjectMapper();
    assertEquals(
        "{\"amount\":12.3,\"precise\":12.3,\"tax\":1.2,\"quantity\":2}",
        plain.writeValueAsString(new Price()));
    assertEquals("{\"value\":42}", plain.writeValueAsString(new InvalidPattern()));
    assertEquals("{\"value\":\"custom\"}", plain.writeValueAsString(new CustomValue()));
    for (ObjectMapper mapper : mappers()) {
      try {
        mapper.writeValueAsString(new DefaultValue());
        fail("Missing pattern should fail");
      } catch (JsonMappingException expected) {
        assertTrue(expected.getMessage().contains("explicit pattern"));
      }
    }
    try {
      plain.writeValueAsString(new ExplicitSerializer());
      fail("Missing module should fail for explicit serializer");
    } catch (JsonMappingException expected) {
      assertTrue(expected.getMessage().contains("no default (no arg) constructor"));
    }
    ObjectMapper mapper = new ObjectMapper().registerModule(new FastPrintfModule());
    assertEquals("{\"value\":\"12.30\"}", mapper.writeValueAsString(new ExplicitSerializer()));
    assertEquals("{\"value\":\"12.30\"}", mapper.writeValueAsString(new DefaultJsonSerialize()));
  }

  private static Object field(Object object, String name) throws Exception {
    Field field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return field.get(object);
  }

  public static class Price {
    @PrintfFormat("%.2f")
    public BigDecimal amount = new BigDecimal("12.3");

    @PrintfFormat("%.4f")
    public BigDecimal precise = new BigDecimal("12.3");

    @PrintfFormat("%.2f")
    public BigDecimal tax = new BigDecimal("1.2");

    public int quantity = 2;
  }

  public static class GetterValue {
    @PrintfFormat("%#06x")
    public int getCode() {
      return 42;
    }
  }

  public static class TextValue {
    @PrintfFormat("[%s]")
    public String text = "quote\" slash\\ line\n";
  }

  public static class DefaultValue {
    @JsonSerialize(using = FastPrintfSerializer.class)
    public int value = 42;
  }

  public static class CustomValue {
    @PrintfFormat("%.2f")
    @JsonSerialize(using = CustomSerializer.class)
    public BigDecimal value = BigDecimal.ONE;
  }

  public static class CustomSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString("custom");
    }
  }

  public static class UnformattedCustomValue {
    @JsonSerialize(using = CustomSerializer.class)
    public BigDecimal value = BigDecimal.ONE;
  }

  public abstract static class ConflictingMixin {
    @JsonSerialize(using = CustomSerializer.class)
    public BigDecimal amount;
  }

  @Test
  public void rejectsConflictingSerializers() throws Exception {
    for (ObjectMapper mapper : mappers()) {
      assertEquals(
          "{\"value\":\"custom\"}", mapper.writeValueAsString(new UnformattedCustomValue()));
      try {
        mapper.writeValueAsString(new CustomValue());
        fail("Conflicting serializer should fail");
      } catch (JsonMappingException expected) {
        assertTrue(expected.getMessage().contains("@PrintfFormat conflicts"));
        assertTrue(expected.getMessage().contains(CustomSerializer.class.getName()));
      }
      mapper.addMixIn(Price.class, ConflictingMixin.class);
      try {
        mapper.writeValueAsString(new Price());
        fail("Conflicting mix-in serializer should fail");
      } catch (JsonMappingException expected) {
        assertTrue(expected.getMessage().contains("@PrintfFormat conflicts"));
      }
    }
  }

  public static class InvalidPattern {
    @PrintfFormat("%?")
    public int value = 42;
  }

  public static class IncompatibleValue {
    @PrintfFormat("%d")
    public String value = "not a number";
  }

  public static class PlainPrice {
    public BigDecimal amount = new BigDecimal("12.3");
  }

  public abstract static class PriceMixin {
    @PrintfFormat("%.2f")
    public BigDecimal amount;
  }

  @Test
  public void supportsAnnotationsWithModule() throws Exception {
    for (ObjectMapper mapper : mappers()) {
      JsonNode json = mapper.readTree(mapper.writeValueAsString(new Price()));
      assertEquals("12.30", json.get("amount").textValue());
      assertEquals("12.3000", json.get("precise").textValue());
      assertEquals("1.20", json.get("tax").textValue());
      assertTrue(json.get("quantity").isInt());
      assertEquals(
          "0x002a",
          mapper.readTree(mapper.writeValueAsString(new GetterValue())).get("code").textValue());
    }
  }

  @Test
  public void preservesNullAndJacksonInclusionPolicy() throws Exception {
    for (ObjectMapper mapper : mappers()) {
      Price price = new Price();
      price.amount = null;
      assertTrue(mapper.readTree(mapper.writeValueAsString(price)).get("amount").isNull());
      ObjectMapper excludingNull =
          mapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
      assertFalse(excludingNull.readTree(excludingNull.writeValueAsString(price)).has("amount"));
    }
  }

  @Test
  public void escapesFormattedTextThroughJackson() throws Exception {
    for (ObjectMapper mapper : mappers()) {
      TextValue value = new TextValue();
      assertEquals(
          "[" + value.text + "]",
          mapper.readTree(mapper.writeValueAsString(value)).get("text").textValue());
    }
  }

  @Test
  public void supportsMixins() throws Exception {
    for (ObjectMapper mapper : mappers()) {
      mapper.addMixIn(PlainPrice.class, PriceMixin.class);
      assertEquals("{\"amount\":\"12.30\"}", mapper.writeValueAsString(new PlainPrice()));
    }
  }

  @Test
  public void reportsInvalidPatternsAndIncompatibleValues() throws Exception {
    for (ObjectMapper mapper : mappers()) {
      try {
        mapper.writeValueAsString(new InvalidPattern());
        fail("Invalid pattern should fail");
      } catch (PrintfSyntaxException | JsonMappingException expected) {
        assertTrue(expected.getMessage().contains("%?"));
      }
      try {
        mapper.writeValueAsString(new IncompatibleValue());
        fail("Incompatible value should fail");
      } catch (JsonMappingException expected) {
        assertEquals("value", expected.getPath().get(0).getFieldName());
      }
    }
  }

  @Test
  public void cachesByPatternWithinModuleAcrossMappers() throws Exception {
    FastPrintfModule module = new FastPrintfModule();
    ObjectMapper first = new ObjectMapper().registerModule(module);
    ObjectMapper second = new ObjectMapper().registerModule(module);
    first.writeValueAsString(new Price());
    Map<String, FastPrintf> cache = cache(module);
    assertEquals(2, cache.size());
    FastPrintf format = cache.get("%.2f");
    assertNotNull(format);
    second.writeValueAsString(new Price());
    assertEquals(2, cache.size());
    assertSame(format, cache.get("%.2f"));

    FastPrintfModule independent = new FastPrintfModule();
    assertTrue(cache(independent).isEmpty());
    new ObjectMapper().registerModule(independent).writeValueAsString(new Price());
    assertNotSame(format, cache(independent).get("%.2f"));
  }

  @Test
  public void safelySharesPatternsDuringConcurrentSerializerCreation() throws Exception {
    FastPrintfModule module = new FastPrintfModule();
    ExecutorService executor = Executors.newFixedThreadPool(4);
    try {
      List<Callable<String>> tasks = new ArrayList<>();
      for (int i = 0; i < 16; i++) {
        tasks.add(() -> new ObjectMapper().registerModule(module).writeValueAsString(new Price()));
      }
      List<Future<String>> results = executor.invokeAll(tasks);
      String expected = results.get(0).get();
      for (Future<String> result : results) assertEquals(expected, result.get());
      assertEquals(2, cache(module).size());
    } finally {
      executor.shutdownNow();
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, FastPrintf> cache(FastPrintfModule module) throws Exception {
    return (Map<String, FastPrintf>) field(field(module, "factory"), "formatters");
  }

  private static ObjectMapper[] mappers() {
    return new ObjectMapper[] {new ObjectMapper().registerModule(new FastPrintfModule())};
  }
}
