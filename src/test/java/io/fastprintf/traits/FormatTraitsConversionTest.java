package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import org.junit.Test;

public class FormatTraitsConversionTest {
  @Test
  public void dispatchesTypesAndPreservesReferences() {
    Object[] values = {
      true,
      'X',
      (byte) 7,
      (short) 300,
      1000,
      2000L,
      1.25f,
      2.5d,
      new StringBuilder("text"),
      new BigInteger("12345678901234567890"),
      new BigDecimal("12.300"),
      Instant.EPOCH,
      new Object(),
      new ObjectTraits("custom")
    };
    Class<?>[] types = {
      BooleanTraits.class,
      CharacterTraits.class,
      ByteTraits.class,
      ShortTraits.class,
      IntTraits.class,
      LongTraits.class,
      FloatTraits.class,
      DoubleTraits.class,
      CharSequenceTraits.class,
      BigIntegerTraits.class,
      BigDecimalTraits.class,
      TemporalAccessorTraits.class,
      ObjectTraits.class,
      ObjectTraits.class
    };
    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      FormatTraits traits = FormatTraits.convert(value);
      assertEquals(types[i], traits.getClass());
      if (value instanceof FormatTraits) {
        assertSame(value, traits);
      } else {
        assertSame(value, traits.asObject());
        assertFalse(traits.ref().isPrimitive());
        assertSame(value, traits.ref().get());
      }
      Args single = Args.exactlyOne(value);
      Args mutable = Args.create().put(value);
      assertSame(traits.asObject(), single.values().get(0));
      for (String pattern : new String[] {"%s", "%p"}) {
        FastPrintf formatter = FastPrintf.compile(pattern);
        assertEquals(formatter.format(mutable), formatter.format(single));
      }
    }
    assertSame(NullTraits.getInstance(), FormatTraits.convert(null));
  }

  @Test
  public void typedBoxedInsertionPreservesTypeAndIdentity() {
    Boolean bool = Boolean.TRUE;
    Character character = 'X';
    Byte small = 7;
    Short shortValue = 300;
    Integer integer = 1000;
    Long longValue = 2000L;
    Float floatValue = 1.25f;
    Double doubleValue = 2.5d;
    Object[] expected = {
      bool, character, small, shortValue, integer, longValue, floatValue, doubleValue
    };
    Args args =
        Args.create()
            .putBooleanOrNull(bool)
            .putCharOrNull(character)
            .putByteOrNull(small)
            .putShortOrNull(shortValue)
            .putIntOrNull(integer)
            .putLongOrNull(longValue)
            .putFloatOrNull(floatValue)
            .putDoubleOrNull(doubleValue);
    int index = 0;
    for (FormatTraits traits : args) {
      Object value = expected[index++];
      assertEquals(FormatTraits.convert(value).getClass(), traits.getClass());
      assertSame(value, traits.asObject());
      assertSame(value, traits.ref().get());
      assertFalse(traits.ref().isPrimitive());
    }
    assertEquals(expected.length, index);
  }

  @Test
  public void nullableInsertionUsesNullTraits() {
    Args args =
        Args.create()
            .putBooleanOrNull(null)
            .putCharOrNull(null)
            .putByteOrNull(null)
            .putShortOrNull(null)
            .putIntOrNull(null)
            .putLongOrNull(null)
            .putFloatOrNull(null)
            .putDoubleOrNull(null)
            .putCharSequence(null)
            .putBigInteger(null)
            .putBigDecimal(null)
            .putDateTime(null);
    assertEquals(12, args.values().size());
    for (FormatTraits value : args) assertSame(NullTraits.getInstance(), value);
  }
}
