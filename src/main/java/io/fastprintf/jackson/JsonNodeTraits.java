package io.fastprintf.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.RefSlot;
import java.math.BigDecimal;

/**
 * Optional Jackson 2 tree-model adapter, selected automatically by {@code Args.put(Object)}.
 *
 * <p>Text nodes provide unquoted text; containers provide JSON. Only numeric nodes support numeric
 * conversions: strings and booleans are not silently coerced to numbers. Decimal and big-integer
 * nodes retain their precision. The original node is retained for object identity and is not
 * copied; callers must not mutate it concurrently with formatting. JSON null follows Java null
 * semantics, including object identity. Missing nodes are rejected.
 */
public final class JsonNodeTraits implements FormatTraits {
  private final JsonNode value;

  public JsonNodeTraits(JsonNode value) {
    this.value = value;
    if (value.isMissingNode()) {
      throw new PrintfException("Missing JSON node cannot be formatted");
    }
  }

  @Override
  public boolean isNull() {
    return value.isNull();
  }

  private void requireNumber() {
    if (!value.isNumber()) {
      throw new PrintfException(
          "JSON %s node cannot be converted to a number", value.getNodeType());
    }
  }

  @Override
  public IntForm asIntForm() {
    requireNumber();
    switch (value.numberType()) {
      case INT:
        return IntForm.valueOf(value.intValue());
      case BIG_INTEGER:
        return IntForm.valueOf(value.bigIntegerValue());
      case BIG_DECIMAL:
        return IntForm.valueOf(value.decimalValue().toBigInteger());
      default:
        return IntForm.valueOf(value.longValue());
    }
  }

  @Override
  public FloatForm asFloatForm() {
    requireNumber();
    if (value.isBigDecimal()) {
      return FloatForm.valueOf(value.decimalValue());
    }
    if (value.isBigInteger()) {
      return FloatForm.valueOf(new BigDecimal(value.bigIntegerValue()));
    }
    return FloatForm.valueOf(value.doubleValue());
  }

  @Override
  public int asInt() {
    requireNumber();
    return value.intValue();
  }

  @Override
  public String asString() {
    return value.isValueNode() ? value.asText() : value.toString();
  }

  @Override
  public char asChar() {
    if (value.isTextual()) {
      String text = value.textValue();
      if (text.isEmpty()) {
        throw new PrintfException("Empty JSON string cannot be converted to char");
      }
      return text.charAt(0);
    }
    return FormatTraits.super.asChar();
  }

  @Override
  public RefSlot ref() {
    return value.isNull() ? RefSlot.ofNull() : RefSlot.of(value);
  }
}
