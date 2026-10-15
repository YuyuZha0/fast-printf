package io.fastprintf;

import static org.junit.Assert.*;

import io.fastprintf.traits.FormatTraits;
import io.fastprintf.traits.ObjectTraits;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.junit.Test;

public class OneArgTest {
  @Test
  public void treatsArraysAndIterablesAsOneArgument() {
    Object[] array = {"first", "second"};
    List<String> list = Arrays.asList("first", "second");
    Object[] values = {array, list, new int[] {1, 2}};
    String[] expected = {"[first, second]", "[first, second]", "[1, 2]"};
    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      Args args = Args.exactlyOne(value);
      assertEquals(1, args.values().size());
      assertSame(value, args.values().get(0));
      Iterator<FormatTraits> iterator = args.iterator();
      assertSame(value, iterator.next().asObject());
      assertFalse(iterator.hasNext());
      assertEquals(expected[i], FastPrintf.compile("%s").format(args));
    }
  }

  @Test
  public void preservesNullAndExistingTraits() {
    Args nil = Args.exactlyOne(null);
    assertEquals(Arrays.asList((Object) null), nil.values());
    assertTrue(nil.iterator().next().isNull());
    assertEquals("[null]", nil.toString());
    assertEquals("null", FastPrintf.compile("%s").format(nil));
    Object value = new Object();
    FormatTraits traits = new ObjectTraits(value);
    Args wrapped = Args.exactlyOne(traits);
    assertSame(traits, wrapped.iterator().next());
    assertSame(value, wrapped.values().get(0));
  }

  @Test
  public void supportsIndependentAndExhaustedIterators() {
    for (Object value : new Object[] {null, "text"}) {
      Args args = Args.exactlyOne(value);
      Iterator<FormatTraits> first = args.iterator();
      Iterator<FormatTraits> second = args.iterator();
      assertTrue(first.hasNext());
      assertTrue(first.hasNext());
      assertSame(first.next(), second.next());
      assertFalse(first.hasNext());
      assertFalse(second.hasNext());
      assertThrows(NoSuchElementException.class, first::next);
      assertThrows(NoSuchElementException.class, first::next);
      assertThrows(UnsupportedOperationException.class, second::remove);
      Iterator<FormatTraits> fresh = args.iterator();
      assertThrows(UnsupportedOperationException.class, fresh::remove);
      assertTrue(fresh.hasNext());
      int visits = 0;
      for (FormatTraits outer : args) {
        for (FormatTraits inner : args) {
          assertSame(outer, inner);
          visits++;
        }
      }
      assertEquals(1, visits);
      int[] remaining = {0};
      fresh.forEachRemaining(traits -> remaining[0]++);
      fresh.forEachRemaining(traits -> remaining[0]++);
      assertEquals(1, remaining[0]);
      assertFalse(fresh.hasNext());
    }
  }

  @Test
  public void reusesArgumentAcrossFormatsAndAppendables() {
    Args args = Args.exactlyOne(42);
    FastPrintf formatter = FastPrintf.compile("%04d");
    assertEquals("0042", formatter.format(args));
    assertEquals("0042", formatter.format(args));
    assertEquals("prefix0042", formatter.format(new StringBuilder("prefix"), args).toString());
    assertEquals("[42]", args.toString());
  }

  @Test
  public void rejectsEveryAppendOperationAndValuesMutation() {
    Args args = Args.exactlyOne("original");
    List<Consumer<Args>> operations =
        Arrays.asList(
            Args::putNull,
            a -> a.putBoolean(true),
            a -> a.putBooleanOrNull(null),
            a -> a.putChar('a'),
            a -> a.putCharOrNull(null),
            a -> a.putByte((byte) 1),
            a -> a.putByteOrNull(null),
            a -> a.putShort((short) 1),
            a -> a.putShortOrNull(null),
            a -> a.putInt(1),
            a -> a.putIntOrNull(null),
            a -> a.putLong(1),
            a -> a.putLongOrNull(null),
            a -> a.putFloat(1),
            a -> a.putFloatOrNull(null),
            a -> a.putDouble(1),
            a -> a.putDoubleOrNull(null),
            a -> a.putDateTime(Instant.EPOCH),
            a -> a.putCharSequence("text"),
            a -> a.putString("text"),
            a -> a.putBigInteger(BigInteger.ONE),
            a -> a.putBigDecimal(BigDecimal.ONE),
            a -> a.put(new Object()));
    for (Consumer<Args> operation : operations) {
      assertThrows(UnsupportedOperationException.class, () -> operation.accept(args));
      assertEquals(Arrays.asList("original"), args.values());
    }
    assertThrows(UnsupportedOperationException.class, () -> args.values().add("other"));
    assertThrows(UnsupportedOperationException.class, () -> args.values().set(0, "other"));
    assertThrows(UnsupportedOperationException.class, () -> args.values().remove(0));
    assertEquals("original", args.iterator().next().asObject());
  }
}
