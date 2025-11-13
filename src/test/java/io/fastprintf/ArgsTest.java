package io.fastprintf;

import static org.junit.Assert.*;

import io.fastprintf.traits.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class ArgsTest {

  @Test
  public void testOfVarargs_withValues() {
    Args args = Args.of(123, "test", true, 3.14);
    List<Object> expected = Arrays.asList(123, "test", true, 3.14);
    assertEquals(expected, args.values());
  }

  @Test
  public void testOfVarargs_withNullsInside() {
    Args args = Args.of(123, null, "test");
    List<Object> expected = Arrays.asList(123, null, "test");
    assertEquals(expected, args.values());
  }

  @Test
  public void testOfVarargs_withEmptyArray() {
    Args args = Args.of();
    assertTrue(args.values().isEmpty());
  }

  @Test
  public void testOfVarargs_withNullArray() {
    // As per Javadoc, a null varargs array should be treated as a single null argument.
    Args args = Args.of((Object[]) null);
    List<Object> expected = Collections.singletonList(null);
    assertEquals(1, args.values().size());
    assertEquals(expected, args.values());
  }

  @Test
  public void testOfIterable() {
    List<Object> sourceList = Arrays.asList("a", 1, BigDecimal.TEN);
    Args args = Args.of(sourceList);
    assertEquals(sourceList, args.values());
  }

  @Test
  public void testOfIterable_thatIsNotACollection() {
    // Create an iterable that doesn't have a known size
    Iterable<Integer> source = () -> Arrays.asList(1, 2, 3).iterator();
    Args args = Args.of(source);
    assertEquals(Arrays.asList(1, 2, 3), args.values());
  }

  @Test(expected = NullPointerException.class)
  public void testOfIterable_withNull() {
    Args.of((Iterable<?>) null);
  }

  @Test
  public void testCreate() {
    Args args = Args.create();
    assertTrue(args.values().isEmpty());
  }

  @Test
  public void testCreateWithExpectedSize() {
    Args args = Args.createWithExpectedSize(5);
    assertTrue(args.values().isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateWithExpectedSize_negative() {
    Args.createWithExpectedSize(-1);
  }

  @Test
  public void testFluentBuilder_primitives() {
    Args args =
        Args.create()
            .putInt(1)
            .putLong(2L)
            .putDouble(3.0)
            .putBoolean(true)
            .putChar('a')
            .putFloat(4.0f)
            .putShort((short) 5)
            .putByte((byte) 6);

    List<Object> expected = Arrays.asList(1, 2L, 3.0, true, 'a', 4.0f, (short) 5, (byte) 6);
    assertEquals(expected, args.values());
  }

  @Test
  public void testFluentBuilder_objectsAndNulls() {
    Args args =
        Args.create()
            .putIntOrNull(10)
            .putLongOrNull(null)
            .putString("hello")
            .putBigDecimal(BigDecimal.ONE)
            .putBigInteger(BigInteger.ZERO)
            .putDateTime(LocalDate.of(2023, 1, 1))
            .putNull();

    List<Object> expected =
        Arrays.asList(
            10, null, "hello", BigDecimal.ONE, BigInteger.ZERO, LocalDate.of(2023, 1, 1), null);
    assertEquals(expected, args.values());
  }

  @Test
  public void testIterator_producesCorrectTraitTypes() {
    Args args =
        Args.of(
            (byte) 1,
            (short) 2,
            3,
            4L,
            5.0f,
            6.0,
            true,
            'c',
            "string",
            new BigDecimal("1.0"),
            new BigInteger("2"),
            LocalDate.now(),
            null,
            new Object());

    Iterator<FormatTraits> it = args.iterator();
    assertTrue(it.next() instanceof ByteTraits);
    assertTrue(it.next() instanceof ShortTraits);
    assertTrue(it.next() instanceof IntTraits);
    assertTrue(it.next() instanceof LongTraits);
    assertTrue(it.next() instanceof FloatTraits);
    assertTrue(it.next() instanceof DoubleTraits);
    assertTrue(it.next() instanceof BooleanTraits);
    assertTrue(it.next() instanceof CharacterTraits);
    assertTrue(it.next() instanceof CharSequenceTraits);
    assertTrue(it.next() instanceof BigDecimalTraits);
    assertTrue(it.next() instanceof BigIntegerTraits);
    assertTrue(it.next() instanceof TemporalAccessorTraits);
    assertTrue(it.next() instanceof NullTraits);
    assertTrue(it.next() instanceof ObjectTraits);
    assertFalse(it.hasNext());
  }
}
