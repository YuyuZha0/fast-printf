package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.seq.Seq;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import org.junit.Test;

public class IntTraitsTest {

  @Test
  public void testOfPrimitive_Caching() {
    // Values within the cache range [-128, 127] should return the same instance.
    for (int i = -128; i <= 127; i++) {
      IntTraits traits1 = IntTraits.ofPrimitive(i);
      IntTraits traits2 = IntTraits.ofPrimitive(i);
      assertSame("Expected cached instance for value: " + i, traits1, traits2);
    }
  }

  @Test
  public void testOfPrimitive_OutsideCache() {
    // Values outside the cache range should return new instances.
    IntTraits traits1 = IntTraits.ofPrimitive(128);
    IntTraits traits2 = IntTraits.ofPrimitive(128);
    assertNotSame("Expected new instance for value 128", traits1, traits2);
    assertEquals(traits1.asInt(), traits2.asInt());

    IntTraits traits3 = IntTraits.ofPrimitive(-129);
    IntTraits traits4 = IntTraits.ofPrimitive(-129);
    assertNotSame("Expected new instance for value -129", traits3, traits4);
    assertEquals(traits3.asInt(), traits4.asInt());
  }

  @Test
  public void testConversions_Primitive() {
    IntTraits traits = IntTraits.ofPrimitive(65); // 'A'

    assertEquals(65, traits.asInt());
    assertEquals("65", traits.asString());
    assertEquals('A', traits.asChar());

    assertNotNull(traits.asIntForm());
    assertEquals("65", traits.asIntForm().toDecimalString());

    assertNotNull(traits.asFloatForm());
    assertEquals("65.0", traits.asFloatForm().toString()); // DoubleWrapper.toString()
  }

  @Test
  public void testConversions_Boxed() {
    Integer boxedValue = new Integer(-500);
    IntTraits traits = new IntTraits(boxedValue, RefSlot.of(boxedValue));

    assertEquals(-500, traits.asInt());
    assertEquals("-500", traits.asString());
    assertEquals((char) -500, traits.asChar()); // Wraps around

    assertNotNull(traits.asIntForm());
    assertEquals("500", traits.asIntForm().toDecimalString());
    assertEquals(-1, traits.asIntForm().signum());

    assertNotNull(traits.asFloatForm());
    assertEquals("-500.0", traits.asFloatForm().toString());
  }

  @Test
  public void testAsTemporalAccessor_Positive() {
    // A positive int is treated as epoch seconds.
    int epochSeconds = 1640995200; // Jan 1, 2022 00:00:00 GMT
    IntTraits traits = IntTraits.ofPrimitive(epochSeconds);

    TemporalAccessor accessor = traits.asTemporalAccessor();
    Instant expected = Instant.ofEpochSecond(epochSeconds);

    assertEquals(expected, accessor);
  }

  @Test
  public void testAsTemporalAccessor_Negative() {
    // A negative int is treated as an *unsigned* long for epoch seconds.
    // This is a critical behavior to test.
    int negativeValue = -1;
    IntTraits traits = IntTraits.ofPrimitive(negativeValue);

    // Integer.toUnsignedLong(-1) results in 2^32 - 1
    long unsignedEpochSeconds = 4294967295L;
    assertEquals(unsignedEpochSeconds, Integer.toUnsignedLong(negativeValue));

    TemporalAccessor accessor = traits.asTemporalAccessor();
    Instant expected = Instant.ofEpochSecond(unsignedEpochSeconds);

    assertEquals(expected, accessor);
  }

  @Test
  public void testAsTemporalAccessor_Zero() {
    IntTraits traits = IntTraits.ofPrimitive(0);
    TemporalAccessor accessor = traits.asTemporalAccessor();
    assertEquals(Instant.EPOCH, accessor);
  }

  @Test
  public void testRef_Primitive() {
    IntTraits traits = IntTraits.ofPrimitive(100);
    RefSlot ref = traits.ref();

    assertNotNull(ref);
    assertTrue("Expected primitive RefSlot", ref.isPrimitive());
  }

  @Test
  public void testRef_Boxed() {
    Integer boxedValue = new Integer(200);
    IntTraits traits = new IntTraits(boxedValue, RefSlot.of(boxedValue));
    RefSlot ref = traits.ref();

    assertNotNull(ref);
    assertFalse("Expected non-primitive RefSlot", ref.isPrimitive());
    assertSame("Expected ref to hold the original object", boxedValue, ref.get());
  }

  @Test
  public void testAsObject() {
    // For primitive traits, asObject() should return a boxed Integer equal to the value.
    IntTraits primitiveTraits = IntTraits.ofPrimitive(123);
    Object obj1 = primitiveTraits.asObject();
    assertTrue(obj1 instanceof Integer);
    assertEquals(123, obj1);

    // For boxed traits, asObject() must return the *original* object instance.
    Integer originalBoxedInt = new Integer(456);
    IntTraits boxedTraits = new IntTraits(originalBoxedInt, RefSlot.of(originalBoxedInt));
    Object obj2 = boxedTraits.asObject();
    assertSame("asObject() must return the identical boxed instance", originalBoxedInt, obj2);
  }

  @Test
  public void testAsSeq_StandardValues() {
    verifyAsSeq(0);
    verifyAsSeq(1);
    verifyAsSeq(9);
    verifyAsSeq(10);
    verifyAsSeq(99);
    verifyAsSeq(100);
    verifyAsSeq(123456789);
    verifyAsSeq(-1);
    verifyAsSeq(-9);
    verifyAsSeq(-10);
    verifyAsSeq(-12345);
  }

  @Test
  public void testAsSeq_EdgeCases() {
    // Critical: Integer.MIN_VALUE (-2147483648) has specific length logic
    verifyAsSeq(Integer.MIN_VALUE);
    verifyAsSeq(Integer.MAX_VALUE);
  }

  private void verifyAsSeq(int value) {
    IntTraits traits = IntTraits.ofPrimitive(value);
    Seq seq = traits.asSeq();
    String expected = Integer.toString(value);

    // 1. Verify Length (Checks if Utils.stringSize matches StringBuilder logic)
    assertEquals("Length mismatch for " + value, expected.length(), seq.length());

    // 2. Verify Content (Triggers LazySeq execution and validation)
    assertEquals("Content mismatch for " + value, expected, seq.toString());

    // 3. Verify Fast Path Append
    StringBuilder sb = new StringBuilder();
    seq.appendTo(sb);
    assertEquals(expected, sb.toString());
  }
}
