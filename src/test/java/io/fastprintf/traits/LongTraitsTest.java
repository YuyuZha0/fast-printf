package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.number.FloatLayout;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import org.junit.Test;

public class LongTraitsTest {

  @Test
  public void testOfPrimitive_createsPrimitiveTraits() {
    long primitiveValue = 1234567890L;
    LongTraits traits = LongTraits.ofPrimitive(primitiveValue);

    assertEquals(primitiveValue, Long.parseLong(traits.asIntForm().toString()));
    assertTrue("ref() should indicate a primitive source", traits.ref().isPrimitive());

    Object obj = traits.asObject();
    assertTrue("asObject() should return a boxed Long", obj instanceof Long);
    assertEquals(primitiveValue, ((Long) obj).longValue());
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    Long boxedValue = new Long(-9876543210L);
    LongTraits traits = new LongTraits(boxedValue, RefSlot.of(boxedValue));

    assertEquals(boxedValue.longValue(), Long.parseLong(traits.asIntForm().toString()));
    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testAsString() {
    assertEquals("12345", LongTraits.ofPrimitive(12345L).asString());
    assertEquals("-98765", LongTraits.ofPrimitive(-98765L).asString());
    assertEquals("0", LongTraits.ofPrimitive(0L).asString());
  }

  @Test
  public void testAsInt_truncatesValue() {
    // Test a value that fits
    assertEquals(12345, LongTraits.ofPrimitive(12345L).asInt());

    // Test a value that is too large and will be truncated
    long largeValue = (long) Integer.MAX_VALUE + 1; // 2147483648L
    int expectedTruncatedValue = (int) largeValue; // Should be Integer.MIN_VALUE
    assertEquals(Integer.MIN_VALUE, expectedTruncatedValue);
    assertEquals(expectedTruncatedValue, LongTraits.ofPrimitive(largeValue).asInt());
  }

  @Test
  public void testAsChar_truncatesValue() {
    // asChar inherits from asInt(), which truncates.
    // 65536 is 2^16, so casting it to char results in 0.
    assertEquals('\0', LongTraits.ofPrimitive(65536L).asChar());
    // 65537 should truncate to 1.
    assertEquals((char) 1, LongTraits.ofPrimitive(65537L).asChar());
    // 'A'
    assertEquals('A', LongTraits.ofPrimitive(65L).asChar());
  }

  @Test
  public void testAsIntForm() {
    long value = -9876543210987L;
    IntForm intForm = LongTraits.ofPrimitive(value).asIntForm();
    assertEquals(String.valueOf(value), intForm.toString());
    assertEquals(-1, intForm.signum());
  }

  @Test
  public void testAsFloatForm() {
    long value = 12345L;
    FloatLayout layout = LongTraits.ofPrimitive(value).asFloatForm().decimalLayout(1);
    assertEquals("12345", layout.getMantissa().toString());
  }

  @Test
  public void testAsTemporalAccessor_asSeconds() {
    // A value within the unsigned int range is treated as seconds
    long epochSeconds = 1640995200L;
    TemporalAccessor accessor = LongTraits.ofPrimitive(epochSeconds).asTemporalAccessor();
    assertEquals(Instant.ofEpochSecond(epochSeconds), accessor);
  }

  @Test
  public void testAsTemporalAccessor_asMillis() {
    // A value larger than MAX_UNSIGNED_INT is treated as millis
    long epochMillis = (Integer.toUnsignedLong(-1)) + 1000L;
    TemporalAccessor accessor = LongTraits.ofPrimitive(epochMillis).asTemporalAccessor();
    assertEquals(Instant.ofEpochMilli(epochMillis), accessor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAsTemporalAccessor_throwsForNegative() {
    LongTraits.ofPrimitive(-1L).asTemporalAccessor();
  }

  @Test
  public void testEdgeCases_MinValue() {
    long value = Long.MIN_VALUE;
    LongTraits traits = LongTraits.ofPrimitive(value);
    assertEquals(String.valueOf(Long.MIN_VALUE), traits.asString());
    assertEquals((int) Long.MIN_VALUE, traits.asInt()); // (int)MIN_VALUE is 0
    assertEquals(0, traits.asInt());
  }

  @Test
  public void testEdgeCases_MaxValue() {
    long value = Long.MAX_VALUE;
    LongTraits traits = LongTraits.ofPrimitive(value);
    assertEquals(String.valueOf(Long.MAX_VALUE), traits.asString());
    assertEquals((int) Long.MAX_VALUE, traits.asInt()); // (int)MAX_VALUE is -1
    assertEquals(-1, traits.asInt());
  }

  @Test
  public void testAsSeq_StandardValues() {
    // Tests the integration of Utils.stringSize(long) and StringBuilder.append(long)
    verifyAsSeq(0L);
    verifyAsSeq(1L);
    verifyAsSeq(-1L);
    verifyAsSeq(1234567890L);
    verifyAsSeq(-9876543210L);
  }

  @Test
  public void testAsSeq_EdgeCases() {
    // Critical: Long.MIN_VALUE has specific length logic in Utils
    verifyAsSeq(Long.MIN_VALUE);
    verifyAsSeq(Long.MAX_VALUE);
  }

  private void verifyAsSeq(long value) {
    LongTraits traits = LongTraits.ofPrimitive(value);
    Seq seq = traits.asSeq();
    String expected = Long.toString(value);

    // 1. Verify Length (Crucial for LazySeq pre-sizing)
    assertEquals("Length mismatch for " + value, expected.length(), seq.length());

    // 2. Verify Content (Triggers LazySeq execution)
    assertEquals("Content mismatch for " + value, expected, seq.toString());

    // 3. Verify Append (Fast path)
    StringBuilder sb = new StringBuilder();
    seq.appendTo(sb);
    assertEquals(expected, sb.toString());
  }
}
