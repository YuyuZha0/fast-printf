package io.fastprintf.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.RefSlot;
import io.fastprintf.util.Utils;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;

/**
 * Optional Jackson 2 tree-model adapter, selected automatically by {@code Args.put(Object)}.
 *
 * <p>Text nodes provide unquoted text; containers provide JSON. Text and boolean nodes use the same
 * numeric conversions as ordinary string and boolean arguments. Decimal and big-integer nodes
 * retain their precision. The original node is retained for object identity and is not copied;
 * callers must not mutate it concurrently with formatting. The automatic adapter routes JSON null
 * to {@code NullTraits}; this class only accepts non-null, non-missing nodes.
 */
final class JsonNodeTraits implements FormatTraits {
  private final JsonNode value;

  /**
   * Wraps a non-null JSON value while retaining the original node as its reference.
   *
   * @throws PrintfException if the node is a JSON null or missing node
   */
  JsonNodeTraits(JsonNode value) {
    this.value = value;
    if (value.isMissingNode() || value.isNull()) {
      throw new PrintfException("Missing/Null JSON node cannot be formatted");
    }
  }

  private PrintfException cannotConvertToNumber() {
    return new PrintfException("JSON %s node cannot be converted to a number", value.getNodeType());
  }

  private PrintfException cannotParseNumber(String target, NumberFormatException cause) {
    return new PrintfException(
        cause, "Cannot convert JSON text node \"%s\" to %s", value.textValue(), target);
  }

  @Override
  public IntForm asIntForm() {
    if (value.isNumber()) {
      switch (value.numberType()) {
        case INT:
          return IntForm.valueOf(value.intValue());
        case FLOAT:
          return IntForm.valueOf((int) value.floatValue());
        case BIG_INTEGER:
          return IntForm.valueOf(value.bigIntegerValue());
        case BIG_DECIMAL:
          return IntForm.valueOf(value.decimalValue().toBigInteger());
        default:
          // LONG and DOUBLE both convert through longValue(); keep FLOAT's 32-bit form above.
          return IntForm.valueOf(value.longValue());
      }
    } else if (value.isTextual()) {
      try {
        return IntForm.valueOf(Long.parseLong(value.textValue()));
      } catch (NumberFormatException e) {
        throw cannotParseNumber("int", e);
      }
    } else if (value.isBoolean()) {
      return IntForm.valueOf(value.booleanValue() ? 1 : 0);
    } else {
      throw cannotConvertToNumber();
    }
  }

  @Override
  public FloatForm asFloatForm() {
    if (value.isNumber()) {
      switch (value.numberType()) {
        case BIG_INTEGER:
          return FloatForm.valueOf(new BigDecimal(value.bigIntegerValue()));
        case BIG_DECIMAL:
          return FloatForm.valueOf(value.decimalValue());
        default:
          // Primitive numeric types all use FloatForm's double overload.
          return FloatForm.valueOf(value.doubleValue());
      }
    } else if (value.isTextual()) {
      try {
        return FloatForm.valueOf(Double.parseDouble(value.textValue()));
      } catch (NumberFormatException e) {
        throw cannotParseNumber("float", e);
      }
    } else if (value.isBoolean()) {
      return FloatForm.valueOf(value.booleanValue() ? 1.0 : 0.0);
    } else {
      throw cannotConvertToNumber();
    }
  }

  @Override
  public int asInt() {
    if (value.isNumber()) {
      return value.intValue();
    } else if (value.isTextual()) {
      try {
        return Integer.parseInt(value.textValue());
      } catch (NumberFormatException e) {
        throw cannotParseNumber("int", e);
      }
    } else if (value.isBoolean()) {
      return value.booleanValue() ? 1 : 0;
    } else {
      throw cannotConvertToNumber();
    }
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    if (value.isIntegralNumber()) {
      // Match the core integer traits' seconds/milliseconds convention and long conversion.
      return Utils.longToInstant(value.longValue());
    }
    throw new PrintfException(
        "JSON %s node cannot be converted to TemporalAccessor", value.getNodeType());
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
    return (char) asInt();
  }

  @Override
  public RefSlot ref() {
    return RefSlot.of(value);
  }
}
