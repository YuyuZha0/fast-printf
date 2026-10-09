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
    JsonNodeTraits explicit = new JsonNodeTraits(node);
    assertNull(explicit.asObject());
    assertEquals("null", FastPrintf.compile("%p").format(explicit));
  }

  @Test
  public void rejectsSilentNumericCoercionAndMissingNodes() {
    for (JsonNode node :
        new JsonNode[] {
          TextNode.valueOf("12"),
          BooleanNode.TRUE,
          NullNode.getInstance(),
          JsonNodeFactory.instance.objectNode(),
          JsonNodeFactory.instance.arrayNode()
        }) {
      JsonNodeTraits traits = new JsonNodeTraits(node);
      assertThrows(PrintfException.class, traits::asIntForm);
      assertThrows(PrintfException.class, traits::asFloatForm);
      assertThrows(PrintfException.class, traits::asInt);
    }
    assertThrows(PrintfException.class, () -> Args.create().put(MissingNode.getInstance()));
  }

  @Test
  public void supportsCharactersAndDynamicFields() {
    assertEquals("A", format("%c", IntNode.valueOf(65)));
    assertEquals("a", format("%c", TextNode.valueOf("abc")));
    assertThrows(PrintfException.class, () -> format("%c", TextNode.valueOf("")));
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
  public void preservesOriginalNodeIdentity() {
    JsonNode node = TextNode.valueOf("value");
    JsonNodeTraits traits = new JsonNodeTraits(node);
    assertSame(node, traits.ref().get());
    assertSame(node, traits.asObject());
    assertFalse(traits.isNull());
    assertTrue(new JsonNodeTraits(NullNode.getInstance()).isNull());
    assertEquals(
        node.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(node)),
        format("%p", node));
    assertSame(traits, Args.of(traits).iterator().next());
  }
}
