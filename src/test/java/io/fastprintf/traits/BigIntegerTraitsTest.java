package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import org.junit.Test;

public class BigIntegerTraitsTest {

  private static final BigInteger LARGE_POSITIVE = new BigInteger("9876543210987654321");
  private static final BigInteger LARGE_NEGATIVE = new BigInteger("-1234567890123456789");

  @Test
  public void testConstructorAndRef() {
    BigInteger bi = BigInteger.TEN;
    BigIntegerTraits traits = new BigIntegerTraits(bi);

    assertNotNull("Traits should not be null", traits);
    RefSlot ref = traits.ref();
    assertFalse("RefSlot should not be primitive", ref.isPrimitive());
    assertSame("RefSlot should hold the original BigInteger object", bi, ref.get());
  }

  @Test
  public void testAsString() {
    BigIntegerTraits traitsPos = new BigIntegerTraits(LARGE_POSITIVE);
    assertEquals("9876543210987654321", traitsPos.asString());

    BigIntegerTraits traitsNeg = new BigIntegerTraits(LARGE_NEGATIVE);
    assertEquals("-1234567890123456789", traitsNeg.asString());
  }

  @Test
  public void testAsInt() {
    // Value that fits in int
    BigInteger bi1 = BigInteger.valueOf(500);
    BigIntegerTraits traits1 = new BigIntegerTraits(bi1);
    assertEquals(500, traits1.asInt());

    // Value larger than Integer.MAX_VALUE
    BigInteger bi2 = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE); // 2^31
    BigIntegerTraits traits2 = new BigIntegerTraits(bi2);
    // This relies on BigInteger.intValue() returning the low-order 32 bits, which is -2147483648
    // for 2^31.
    assertEquals(bi2.intValue(), traits2.asInt());
  }

  @Test
  public void testAsChar_usesAsInt() {
    // Since asChar() is not overridden, it should default to (char)asInt().
    BigInteger bi = BigInteger.valueOf(65); // ASCII 'A'
    BigIntegerTraits traits = new BigIntegerTraits(bi);
    assertEquals('A', traits.asChar());

    // Test with a value that wraps around when cast to char
    BigInteger biWrap = BigInteger.valueOf(65537); // 65536 (2^16) + 1
    BigIntegerTraits traitsWrap = new BigIntegerTraits(biWrap);
    assertEquals((char) 65537, traitsWrap.asChar()); // Should be (char)1
    assertEquals(1, traitsWrap.asChar());
  }

  @Test
  public void testAsIntForm() {
    BigIntegerTraits traits = new BigIntegerTraits(LARGE_POSITIVE);
    IntForm intForm = traits.asIntForm();

    assertTrue("Should return BigIntegerWrapper (IntForm)", intForm.toString().contains("9876"));
    assertEquals(1, intForm.signum());
  }

  @Test
  public void testAsFloatForm() {
    BigIntegerTraits traits = new BigIntegerTraits(BigInteger.valueOf(500));
    FloatForm floatForm = traits.asFloatForm();

    // The conversion uses 'new BigDecimal(value, 0)'
    assertNotNull("FloatForm should not be null", floatForm);
    // We can check the string representation, which should be the BigDecimal representation
    assertEquals("500", floatForm.toString());
  }

  @Test
  public void testAsTemporalAccessor_EpochSeconds() {
    // Test a value small enough to be treated as epoch seconds
    long epochSeconds = 1640995200L; // Jan 1, 2022
    BigInteger bi = BigInteger.valueOf(epochSeconds);
    BigIntegerTraits traits = new BigIntegerTraits(bi);

    TemporalAccessor accessor = traits.asTemporalAccessor();
    Instant expected = Instant.ofEpochSecond(epochSeconds);

    assertEquals(expected, accessor);
  }

  @Test
  public void testAsTemporalAccessor_EpochMilliseconds() {
    // Test a value large enough to be treated as epoch milliseconds
    // MAX_UNSIGNED_INT is 0xFFFFFFFFL (approx 4.2 billion). We test slightly above that.
    // We rely on Utils.longToInstant logic here.
    long epochMillis = 5_000_000_000L;
    BigInteger bi = BigInteger.valueOf(epochMillis);
    BigIntegerTraits traits = new BigIntegerTraits(bi);

    TemporalAccessor accessor = traits.asTemporalAccessor();
    Instant expected = Instant.ofEpochMilli(epochMillis);

    assertEquals(expected, accessor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAsTemporalAccessor_NegativeValue() {
    // Utils.longToInstant throws IllegalArgumentException for negative long values.
    BigIntegerTraits traits = new BigIntegerTraits(BigInteger.valueOf(-1));
    traits.asTemporalAccessor();
  }

  @Test
  public void testAsObject() {
    BigInteger originalValue = BigInteger.ONE;
    BigIntegerTraits traits = new BigIntegerTraits(originalValue);
    Object obj = traits.asObject();

    assertSame("asObject() must return the identical BigInteger instance", originalValue, obj);
  }
}
