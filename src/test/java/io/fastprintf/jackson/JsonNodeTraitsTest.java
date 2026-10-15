package io.fastprintf.jackson;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import io.fastprintf.PrintfException;
import io.fastprintf.traits.NullTraits;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import org.junit.Test;

public class JsonNodeTraitsTest {
  private static String format(String pattern, JsonNode node) {
    return FastPrintf.compile(pattern).format(node);
  }

  @Test
  public void automaticallyAdaptsTextAndNumbers() {
    assertTrue(Args.create().put(IntNode.valueOf(42)).iterator().next() instanceof JsonNodeTraits);
    assertEquals("Alice", format("%s", TextNode.valueOf("Alice")));
    assertEquals("ALICE", format("%S", TextNode.valueOf("Alice")));
    assertEquals("42", format("%d", IntNode.valueOf(42)));
    assertEquals("ffffffff", format("%x", IntNode.valueOf(-1)));
    assertEquals("ffffffffffffffff", format("%x", LongNode.valueOf(-1)));
    assertEquals("9223372036854775807", format("%d", LongNode.valueOf(Long.MAX_VALUE)));
    assertEquals("12", format("%d", DoubleNode.valueOf(12.75)));
    assertEquals("-12", format("%d", FloatNode.valueOf(-12.75f)));
    assertEquals("1.25", format("%.2f", DoubleNode.valueOf(1.25)));
    assertEquals("Infinity", format("%f", DoubleNode.valueOf(Double.POSITIVE_INFINITY)));
    assertEquals("-0.000000", format("%f", DoubleNode.valueOf(-0.0)));
    assertEquals("NaN", format("%g", DoubleNode.valueOf(Double.NaN)));
  }

  @Test
  public void primitiveNumericNodesMatchTheirJavaTypes() {
    Object[] values = {65, Long.MAX_VALUE, -1.75f, 1.0e10f, 12.75d};
    JsonNode[] nodes = {
      IntNode.valueOf(65),
      LongNode.valueOf(Long.MAX_VALUE),
      FloatNode.valueOf(-1.75f),
      FloatNode.valueOf(1.0e10f),
      DoubleNode.valueOf(12.75)
    };
    for (int i = 0; i < nodes.length; i++) {
      for (String pattern : new String[] {"%d", "%u", "%x", "%.2f", "%g", "%a"}) {
        assertEquals(FastPrintf.compile(pattern).format(values[i]), format(pattern, nodes[i]));
      }
    }
    // Float-to-integer conversion retains the core FloatTraits 32-bit integer semantics.
    assertEquals("ffffffff", format("%x", FloatNode.valueOf(-1.75f)));
    assertEquals("2147483647", format("%d", FloatNode.valueOf(1.0e10f)));
  }

  @Test
  public void integralNodesSupportTemporalConversion() {
    for (long value : new long[] {0, 42, 0xFFFFFFFFL, 0x100000000L, Long.MAX_VALUE}) {
      Instant expected =
          value <= 0xFFFFFFFFL ? Instant.ofEpochSecond(value) : Instant.ofEpochMilli(value);
      for (JsonNode node :
          new JsonNode[] {
            LongNode.valueOf(value), BigIntegerNode.valueOf(BigInteger.valueOf(value))
          }) {
        JsonNodeTraits traits = new JsonNodeTraits(node);
        assertEquals(expected, traits.asTemporalAccessor());
        assertSame(node, traits.ref().get());
        for (String pattern : new String[] {"%t", "%T"}) {
          assertEquals(FastPrintf.compile(pattern).format(value), format(pattern, node));
        }
      }
    }
    assertEquals(
        Instant.ofEpochSecond(42), new JsonNodeTraits(IntNode.valueOf(42)).asTemporalAccessor());
    assertEquals(
        Instant.ofEpochSecond(42),
        new JsonNodeTraits(ShortNode.valueOf((short) 42)).asTemporalAccessor());
    assertEquals(FastPrintf.compile("%t").format(42), format("%t", IntNode.valueOf(42)));
  }

  @Test
  public void temporalConversionRetainsCoreBigIntegerSemantics() {
    BigInteger value = BigInteger.ONE.shiftLeft(64).add(BigInteger.valueOf(42));
    JsonNode node = BigIntegerNode.valueOf(value);
    assertEquals(Instant.ofEpochSecond(42), new JsonNodeTraits(node).asTemporalAccessor());
    assertEquals(FastPrintf.compile("%t").format(value), format("%t", node));
  }

  @Test
  public void rejectsNegativeTimestampsAndNonIntegralTemporalNodes() {
    for (JsonNode node :
        new JsonNode[] {
          IntNode.valueOf(-1),
          LongNode.valueOf(Long.MIN_VALUE),
          BigIntegerNode.valueOf(BigInteger.valueOf(-1))
        }) {
      IllegalArgumentException error =
          assertThrows(IllegalArgumentException.class, () -> format("%t", node));
      assertTrue(error.getMessage().contains("must not be negative"));
    }
    for (JsonNode node :
        new JsonNode[] {
          FloatNode.valueOf(42),
          DoubleNode.valueOf(42),
          DecimalNode.valueOf(new BigDecimal("42")),
          TextNode.valueOf("42"),
          TextNode.valueOf("2026-10-15"),
          BooleanNode.TRUE,
          JsonNodeFactory.instance.objectNode(),
          JsonNodeFactory.instance.arrayNode(),
          BinaryNode.valueOf(new byte[] {42}),
          new POJONode(42)
        }) {
      PrintfException error = assertThrows(PrintfException.class, () -> format("%t", node));
      assertTrue(error.getMessage().contains("TemporalAccessor"));
    }
  }

  @Test
  public void preservesBigNumberPrecision() {
    BigInteger integer = new BigInteger("123456789012345678901234567890");
    assertEquals(integer.toString(), format("%d", BigIntegerNode.valueOf(integer)));
    assertEquals(integer + ".00", format("%.2f", BigIntegerNode.valueOf(integer)));
    BigDecimal decimal = new BigDecimal("123456789012345678901234567890.125");
    assertEquals(decimal.toPlainString(), format("%.3f", DecimalNode.valueOf(decimal)));
    assertEquals(integer.toString(), format("%d", DecimalNode.valueOf(decimal)));
  }

  @Test
  public void preservesTextAndContainerSemantics() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    assertEquals("{\"a\":[1,true]}", format("%s", mapper.readTree("{\"a\":[1,true]}")));
    assertEquals("[1,2]", format("%s", mapper.readTree("[1,2]")));
    assertEquals("true", format("%s", BooleanNode.TRUE));
    assertEquals("null", format("%s", NullNode.getInstance()));
    assertEquals("a\"b\nc", format("%s", TextNode.valueOf("a\"b\nc")));
    assertEquals("null", FastPrintf.compile("%s").format((Object) null));
  }

  @Test
  public void jsonNullUsesTheExistingNullAdapter() {
    NullNode node = NullNode.getInstance();
    assertSame(NullTraits.getInstance(), Args.create().put(node).iterator().next());
    assertEquals("null", format("%p", node));
    assertThrows(PrintfException.class, () -> format("%d", node));
    assertThrows(PrintfException.class, () -> format("%f", node));
    assertTrue(Args.exactlyOne(node).iterator().next().isNull());
    PrintfException error = assertThrows(PrintfException.class, () -> new JsonNodeTraits(node));
    assertEquals("Missing/Null JSON node cannot be formatted", error.getMessage());
  }

  @Test
  public void rejectsInvalidNumericConversionsAndMissingNodes() {
    for (JsonNode node :
        new JsonNode[] {
          JsonNodeFactory.instance.objectNode(),
          JsonNodeFactory.instance.arrayNode(),
          BinaryNode.valueOf(new byte[] {1, 2}),
          new POJONode(42)
        }) {
      JsonNodeTraits traits = new JsonNodeTraits(node);
      for (PrintfException error :
          new PrintfException[] {
            assertThrows(PrintfException.class, traits::asIntForm),
            assertThrows(PrintfException.class, traits::asFloatForm),
            assertThrows(PrintfException.class, traits::asInt),
            assertThrows(PrintfException.class, traits::asChar)
          }) {
        assertEquals(
            "JSON " + node.getNodeType() + " node cannot be converted to a number",
            error.getMessage());
      }
    }
    assertThrows(PrintfException.class, () -> Args.create().put(MissingNode.getInstance()));
    assertEquals(
        "Missing/Null JSON node cannot be formatted",
        assertThrows(PrintfException.class, () -> new JsonNodeTraits(MissingNode.getInstance()))
            .getMessage());
  }

  @Test
  public void numericTextMatchesOrdinaryStringArguments() {
    for (String text :
        new String[] {
          "0", "+42", "-42", "00042", Long.toString(Long.MIN_VALUE), Long.toString(Long.MAX_VALUE)
        }) {
      for (String pattern : new String[] {"%d", "%u", "%#x", "%o", "%.2f", "%e", "%g"}) {
        assertEquals(
            FastPrintf.compile(pattern).format(text), format(pattern, TextNode.valueOf(text)));
      }
    }
    for (String text :
        new String[] {
          "12.75", "-0.0", "1.25e3", " 1.25 ", "0x1.8p1", "NaN", "Infinity", "-Infinity"
        }) {
      for (String pattern : new String[] {"%.2f", "%e", "%g", "%a"}) {
        assertEquals(
            FastPrintf.compile(pattern).format(text), format(pattern, TextNode.valueOf(text)));
      }
    }
    assertEquals("12", format("%s", TextNode.valueOf("12")));
    assertEquals("1", format("%c", TextNode.valueOf("12")));
  }

  @Test
  public void rejectsMalformedTextAndIntegerOverflow() {
    for (String text : new String[] {"", " ", "null", "true", "12x"}) {
      JsonNodeTraits traits = new JsonNodeTraits(TextNode.valueOf(text));
      for (PrintfException error :
          new PrintfException[] {
            assertThrows(PrintfException.class, traits::asIntForm),
            assertThrows(PrintfException.class, traits::asFloatForm),
            assertThrows(PrintfException.class, traits::asInt)
          }) {
        assertTrue(error.getCause() instanceof NumberFormatException);
        assertTrue(error.getMessage().contains("\"" + text + "\""));
      }
    }
    for (String text :
        new String[] {"1.5", "1e2", " 12 ", "9223372036854775808", "-9223372036854775809"}) {
      assertTrue(
          assertThrows(PrintfException.class, () -> format("%d", TextNode.valueOf(text))).getCause()
              instanceof NumberFormatException);
    }
    for (String text : new String[] {"2147483648", "-2147483649"}) {
      assertTrue(
          assertThrows(
                      PrintfException.class,
                      () -> new JsonNodeTraits(TextNode.valueOf(text)).asInt())
                  .getCause()
              instanceof NumberFormatException);
    }
    assertEquals(Integer.MIN_VALUE, new JsonNodeTraits(TextNode.valueOf("-2147483648")).asInt());
    assertEquals(Integer.MAX_VALUE, new JsonNodeTraits(TextNode.valueOf("2147483647")).asInt());
  }

  @Test
  public void floatingTextRetainsDoubleOverflowAndUnderflowSemantics() {
    assertEquals("Infinity", format("%f", TextNode.valueOf("1e309")));
    assertEquals("-Infinity", format("%f", TextNode.valueOf("-1e309")));
    assertEquals("0.000000", format("%f", TextNode.valueOf("1e-4000")));
    assertEquals("-0.000000", format("%f", TextNode.valueOf("-1e-4000")));
  }

  @Test
  public void dynamicFieldsPropagateConversionFailures() {
    for (String pattern : new String[] {"%*f", "%.*f"}) {
      for (String text : new String[] {"invalid", "2147483648", "-2147483649"}) {
        PrintfException error =
            assertThrows(
                PrintfException.class,
                () -> FastPrintf.compile(pattern).format(TextNode.valueOf(text), 1.25));
        assertTrue(error.getCause() instanceof NumberFormatException);
        assertTrue(error.getMessage().contains(text));
      }
    }
  }

  @Test
  public void supportsBooleanNumbersAndTextDynamicFields() {
    for (boolean value : new boolean[] {true, false}) {
      JsonNode node = BooleanNode.valueOf(value);
      for (String pattern : new String[] {"%d", "%f", "%g"}) {
        assertEquals(FastPrintf.compile(pattern).format(value), format(pattern, node));
      }
      assertEquals(value ? 1 : 0, new JsonNodeTraits(node).asInt());
      assertEquals(value ? '\u0001' : '\u0000', new JsonNodeTraits(node).asChar());
      assertEquals(value ? "1.3" : "1", FastPrintf.compile("%.*f").format(node, 1.25));
    }
    assertEquals(
        "  1.25",
        FastPrintf.compile("%*.*f")
            .format(TextNode.valueOf("6"), TextNode.valueOf("2"), TextNode.valueOf("1.25")));
  }

  @Test
  public void supportsCharactersAndDynamicFields() {
    assertEquals("A", format("%c", IntNode.valueOf(65)));
    assertEquals("a", format("%c", TextNode.valueOf("abc")));
    assertEquals(
        "Empty JSON string cannot be converted to char",
        assertThrows(PrintfException.class, () -> format("%c", TextNode.valueOf(""))).getMessage());
    assertEquals(
        "  1.25",
        FastPrintf.compile("%*.*f")
            .format(
                IntNode.valueOf(6),
                IntNode.valueOf(2),
                DecimalNode.valueOf(new BigDecimal("1.25"))));
  }

  @Test
  public void iterableOverloadStillExpandsChildren() {
    ArrayNode array = JsonNodeFactory.instance.arrayNode().add(1).add(2);
    assertEquals("1 2", FastPrintf.compile("%d %d").format(Args.of(array)));
    assertEquals("[1,2]", FastPrintf.compile("%s").format(Args.of((Object) array)));
  }

  @Test
  public void numericConversionsKeepOriginalNodeReference() {
    JsonNode[] nodes = {TextNode.valueOf("42"), BooleanNode.TRUE, BooleanNode.FALSE};
    String[] expected = {"42", "1", "0"};
    for (int i = 0; i < nodes.length; i++) {
      JsonNode node = nodes[i];
      JsonNodeTraits traits = new JsonNodeTraits(node);
      String identity =
          node.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(node));
      assertSame(node, traits.ref().get());
      traits.asIntForm();
      traits.asFloatForm();
      traits.asInt();
      assertFalse(traits.ref().isPrimitive());
      assertSame(node, traits.ref().get());
      assertSame(node, traits.asObject());
      assertEquals(identity, FastPrintf.compile("%p").format(traits));
      assertEquals(node.asText(), traits.asString());
      assertEquals(expected[i], FastPrintf.compile("%d").format(traits));
    }
    JsonNode invalid = TextNode.valueOf("not a number");
    JsonNodeTraits traits = new JsonNodeTraits(invalid);
    assertThrows(PrintfException.class, traits::asIntForm);
    assertThrows(PrintfException.class, traits::asFloatForm);
    assertThrows(PrintfException.class, traits::asInt);
    assertSame(invalid, traits.ref().get());
    assertSame(invalid, traits.asObject());
  }

  @Test
  public void preservesOriginalNodeIdentity() {
    JsonNode node = TextNode.valueOf("value");
    JsonNodeTraits traits = new JsonNodeTraits(node);
    assertSame(node, traits.ref().get());
    assertSame(node, traits.asObject());
    assertFalse(traits.isNull());
    assertEquals(
        node.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(node)),
        format("%p", node));
    assertSame(traits, Args.of(traits).iterator().next());
  }
}
