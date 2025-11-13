package io.fastprintf;

import static org.junit.Assert.*;

import io.fastprintf.traits.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class ArgsImplTest {

  @Test
  public void testEmptyConstructor() {
    ArgsImpl args = new ArgsImpl();
    assertFalse("Iterator should be empty", args.iterator().hasNext());
    assertTrue("Values list should be empty", args.values().isEmpty());
  }

  @Test
  public void testSizedConstructor() {
    // This constructor just pre-allocates, so the behavior is the same as empty.
    ArgsImpl args = new ArgsImpl(10);
    assertFalse("Iterator should be empty", args.iterator().hasNext());
    assertTrue("Values list should be empty", args.values().isEmpty());
  }

  @Test
  public void testPutPrimitives() {
    ArgsImpl args =
        new ArgsImpl()
            .putBoolean(true)
            .putChar('A')
            .putByte((byte) 1)
            .putShort((short) 2)
            .putInt(3)
            .putLong(4L)
            .putFloat(5.0f)
            .putDouble(6.0);

    // Verify the internal traits
    Iterator<FormatTraits> iterator = args.iterator();
    assertTrue(iterator.next() instanceof BooleanTraits);
    assertTrue(iterator.next() instanceof CharacterTraits);
    assertTrue(iterator.next() instanceof ByteTraits);
    assertTrue(iterator.next() instanceof ShortTraits);
    assertTrue(iterator.next() instanceof IntTraits);
    assertTrue(iterator.next() instanceof LongTraits);
    assertTrue(iterator.next() instanceof FloatTraits);
    assertTrue(iterator.next() instanceof DoubleTraits);
    assertFalse(iterator.hasNext());

    // Verify the public values() method
    List<Object> expectedValues =
        Arrays.<Object>asList(true, 'A', (byte) 1, (short) 2, 3, 4L, 5.0f, 6.0);
    assertEquals(expectedValues, args.values());
  }

  @Test
  public void testPutBoxedAndNulls() {
    Integer myInt = 123;
    Double myDouble = 3.14;

    ArgsImpl args =
        new ArgsImpl()
            .putIntOrNull(myInt)
            .putLongOrNull(null)
            .putDoubleOrNull(myDouble)
            .putCharOrNull(null);

    // Verify the internal traits
    Iterator<FormatTraits> iterator = args.iterator();
    assertTrue(iterator.next() instanceof IntTraits);
    assertTrue(iterator.next() instanceof NullTraits);
    assertTrue(iterator.next() instanceof DoubleTraits);
    assertTrue(iterator.next() instanceof NullTraits);
    assertFalse(iterator.hasNext());

    // Verify that the original object references are preserved for non-nulls
    List<Object> values = args.values();
    assertSame(myInt, values.get(0));
    assertNull(values.get(1));
    assertSame(myDouble, values.get(2));
    assertNull(values.get(3));
  }

  @Test
  public void testPutObjectTypes() {
    String string = "hello";
    BigInteger bigInt = BigInteger.valueOf(12345);
    BigDecimal bigDec = BigDecimal.valueOf(1.2345);
    Instant instant = Instant.now();

    ArgsImpl args =
        new ArgsImpl()
            .putCharSequence(string)
            .putBigInteger(bigInt)
            .putBigDecimal(bigDec)
            .putDateTime(instant);

    // Verify the internal traits
    Iterator<FormatTraits> iterator = args.iterator();
    assertTrue(iterator.next() instanceof CharSequenceTraits);
    assertTrue(iterator.next() instanceof BigIntegerTraits);
    assertTrue(iterator.next() instanceof BigDecimalTraits);
    assertTrue(iterator.next() instanceof TemporalAccessorTraits);
    assertFalse(iterator.hasNext());

    // Verify that the original object references are preserved
    List<Object> values = args.values();
    assertSame(string, values.get(0));
    assertSame(bigInt, values.get(1));
    assertSame(bigDec, values.get(2));
    assertSame(instant, values.get(3));
  }

  @Test
  public void testGenericPutDispatch() {
    Object customObject = new Object();
    Instant instant = Instant.EPOCH;

    ArgsImpl args = new ArgsImpl();
    args.put(null);
    args.put(true);
    args.put('c');
    args.put((byte) 10);
    args.put((short) 20);
    args.put(30);
    args.put(40L);
    args.put(50.0f);
    args.put(60.0);
    args.put("a string");
    args.put(BigInteger.TEN);
    args.put(BigDecimal.ONE);
    args.put(instant);
    args.put(customObject);

    // Verify the internal traits to confirm correct dispatch
    Iterator<FormatTraits> iterator = args.iterator();
    assertTrue(iterator.next() instanceof NullTraits);
    assertTrue(iterator.next() instanceof BooleanTraits);
    assertTrue(iterator.next() instanceof CharacterTraits);
    assertTrue(iterator.next() instanceof ByteTraits);
    assertTrue(iterator.next() instanceof ShortTraits);
    assertTrue(iterator.next() instanceof IntTraits);
    assertTrue(iterator.next() instanceof LongTraits);
    assertTrue(iterator.next() instanceof FloatTraits);
    assertTrue(iterator.next() instanceof DoubleTraits);
    assertTrue(iterator.next() instanceof CharSequenceTraits);
    assertTrue(iterator.next() instanceof BigIntegerTraits);
    assertTrue(iterator.next() instanceof BigDecimalTraits);
    assertTrue(iterator.next() instanceof TemporalAccessorTraits);
    assertTrue(iterator.next() instanceof ObjectTraits); // Fallback case
    assertFalse(iterator.hasNext());

    // Check the last two values specifically
    List<Object> values = args.values();
    assertSame(instant, values.get(12));
    assertSame(customObject, values.get(13));
  }

  @Test
  public void testPutFormatTraitsDirectly() {
    FormatTraits customTrait = new IntTraits(999, RefSlot.ofPrimitive());
    ArgsImpl args = new ArgsImpl();
    args.put(customTrait); // Should use putObject() logic

    Iterator<FormatTraits> iterator = args.iterator();
    assertSame(customTrait, iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testValues() {
    Args args = new ArgsImpl().putInt(1).putNull().putString("test").putBoolean(false);

    List<Object> expected = Arrays.<Object>asList(1, null, "test", false);
    assertEquals(expected, args.values());
  }

  @Test
  public void testValuesReturnsCopy() {
    ArgsImpl args = new ArgsImpl().putInt(1);
    List<Object> values1 = args.values();
    List<Object> values2 = args.values();

    assertNotSame(values1, values2);
  }

  @Test
  public void testToString() {
    ArgsImpl emptyArgs = new ArgsImpl();
    assertEquals("[]", emptyArgs.toString());

    ArgsImpl singleArg = new ArgsImpl().putInt(42);
    assertEquals("[42]", singleArg.toString());

    ArgsImpl multipleArgs = new ArgsImpl().putString("hello").putNull().putBoolean(true);
    assertEquals("[hello, null, true]", multipleArgs.toString());
  }
}
