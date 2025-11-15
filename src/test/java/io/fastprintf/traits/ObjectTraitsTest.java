package io.fastprintf.traits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import io.fastprintf.PrintfException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.Test;

public class ObjectTraitsTest {

  // --- asString() Tests ---

  @Test
  public void asString_withStandardObject_shouldCallToString() {
    Object obj =
        new Object() {
          @Override
          public String toString() {
            return "CustomObjectToString";
          }
        };
    ObjectTraits traits = new ObjectTraits(obj);
    assertEquals("CustomObjectToString", traits.asString());
  }

  @Test
  public void asString_emptyArray_shouldReturnEmptyBrackets() {
    ObjectTraits traits = new ObjectTraits(new int[0]);
    assertEquals("[]", traits.asString());
  }

  @Test
  public void asString_withByteArray_shouldReturnBase64() {
    byte[] bytes = new byte[] {1, 2, 3, 4, 5};
    ObjectTraits traits = new ObjectTraits(bytes);
    assertEquals(Base64.getEncoder().encodeToString(bytes), traits.asString());
  }

  @Test
  public void asString_withCharArray_shouldReturnStringRef() {
    char[] chars = new char[] {'h', 'e', 'l', 'l', 'o'};
    ObjectTraits traits = new ObjectTraits(chars);
    assertEquals("hello", traits.asString());
  }

  @Test
  public void asString_withBoolean_shouldReturnStringRef() {
    boolean[] booleans = new boolean[] {true, false, true, false, true};
    ObjectTraits traits = new ObjectTraits(booleans);
    assertEquals(Arrays.toString(booleans), traits.asString());
  }

  @Test
  public void asString_withShortArray_shouldCallArraysToString() {
    short[] shorts = new short[] {100, 200, 300};
    ObjectTraits traits = new ObjectTraits(shorts);
    assertEquals(Arrays.toString(shorts), traits.asString());
  }

  @Test
  public void asString_withIntArray_shouldCallArraysToString() {
    int[] ints = new int[] {10, 20, 30};
    ObjectTraits traits = new ObjectTraits(ints);
    assertEquals(Arrays.toString(ints), traits.asString());
  }

  @Test
  public void asString_withLongArray_shouldCallArraysToString() {
    long[] longs = new long[] {1000L, 2000L, 3000L};
    ObjectTraits traits = new ObjectTraits(longs);
    assertEquals(Arrays.toString(longs), traits.asString());
  }

  @Test
  public void asString_withFloatArray_shouldCallArraysToString() {
    float[] floats = new float[] {1.1f, 2.2f, 3.3f};
    ObjectTraits traits = new ObjectTraits(floats);
    assertEquals(Arrays.toString(floats), traits.asString());
  }

  @Test
  public void asString_withDoubleArray_shouldCallArraysToString() {
    double[] doubles = new double[] {1.11, 2.22, 3.33};
    ObjectTraits traits = new ObjectTraits(doubles);
    assertEquals(Arrays.toString(doubles), traits.asString());
  }

  @Test
  public void asString_withObjectArray_shouldJoinElements() {
    Object[] objects = new Object[] {"one", 2, 3.0};
    ObjectTraits traits = new ObjectTraits(objects);
    assertEquals("[one, 2, 3.0]", traits.asString());
  }

  // --- Numeric Conversion Tests (asIntForm, asFloatForm, asInt) ---

  @Test
  public void asIntForm_withNumber_shouldReturnRefAsLong() {
    ObjectTraits traits = new ObjectTraits(123.45); // Double
    assertEquals("123", traits.asIntForm().toDecimalString());
  }

  @Test
  public void asFloatForm_withNumber_shouldReturnRefAsDouble() {
    ObjectTraits traits = new ObjectTraits(new BigDecimal("123.456"));
    // Using toString to avoid double precision issues in test
    assertEquals("123.456", traits.asFloatForm().toString());
  }

  @Test
  public void asInt_withNumber_shouldReturnRefAsInt() {
    ObjectTraits traits = new ObjectTraits(123.45);
    assertEquals(123, traits.asInt());
  }

  @Test
  public void numericConversions_withNonNumericObject_shouldThrowException() {
    ObjectTraits traits = new ObjectTraits("not a number");
    try {
      traits.asIntForm();
      fail("asIntForm should have thrown PrintfException");
    } catch (PrintfException expected) {
      // Success
    }
    try {
      traits.asFloatForm();
      fail("asFloatForm should have thrown PrintfException");
    } catch (PrintfException expected) {
      // Success
    }
    try {
      traits.asInt();
      fail("asInt should have thrown PrintfException");
    } catch (PrintfException expected) {
      // Success
    }
  }

  // --- Tests for Date and Calendar (Your New Additions) ---

  @Test
  public void asIntForm_withDate_shouldReturnEpochMilliseconds() {
    Date date = new Date(1234567890L); // A fixed time
    ObjectTraits traits = new ObjectTraits(date);
    assertEquals("1234567890", traits.asIntForm().toDecimalString());
  }

  @Test
  public void asFloatForm_withDate_shouldReturnEpochSeconds() {
    Date date = new Date(1234567890L);
    ObjectTraits traits = new ObjectTraits(date);
    // 1234567890 ms = 1234567.890 s
    assertEquals("1234567.89", traits.asFloatForm().toString());
  }

  @Test
  public void asInt_withDate_shouldReturnEpoSeconds() {
    Date date = new Date(1234567890L); // A fixed time
    ObjectTraits traits = new ObjectTraits(date);
    assertEquals(1234567, traits.asInt());
  }

  @Test
  public void asIntForm_withCalendar_shouldReturnEpochMilliseconds() {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(9876543210L);
    ObjectTraits traits = new ObjectTraits(cal);
    assertEquals("9876543210", traits.asIntForm().toDecimalString());
  }

  @Test
  public void asFloatForm_withCalendar_shouldReturnEpochSeconds() {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(9876543210L);
    ObjectTraits traits = new ObjectTraits(cal);
    // 9876543210 ms = 9876543.21 s
    assertEquals("9876543.21", traits.asFloatForm().toString());
  }

  @Test
  public void asInt_withCalendar_shouldReturnEpochSeconds() {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(9876543210L);
    ObjectTraits traits = new ObjectTraits(cal);
    assertEquals(9876543, traits.asInt());
  }

  // --- asTemporalAccessor() Tests ---

  @Test
  public void asTemporalAccessor_withDate_shouldConvertToInstant() {
    Date date = new Date(1234567890L);
    ObjectTraits traits = new ObjectTraits(date);
    assertEquals(date.toInstant(), traits.asTemporalAccessor());
  }

  @Test
  public void asTemporalAccessor_withCalendar_shouldConvertToInstant() {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(9876543210L);
    ObjectTraits traits = new ObjectTraits(cal);
    assertEquals(cal.toInstant(), traits.asTemporalAccessor());
  }

  @Test
  public void asTemporalAccessor_withNumber_shouldConvertToInstant() {
    // Test the seconds heuristic (small number)
    ObjectTraits traitsSeconds = new ObjectTraits(1700000000L);
    assertEquals(Instant.ofEpochSecond(1700000000L), traitsSeconds.asTemporalAccessor());

    // Test the milliseconds heuristic (large number)
    ObjectTraits traitsMillis = new ObjectTraits(1700000000123L);
    assertEquals(Instant.ofEpochMilli(1700000000123L), traitsMillis.asTemporalAccessor());
  }

  @Test(expected = PrintfException.class)
  public void asTemporalAccessor_withInvalidType_shouldThrowException() {
    ObjectTraits traits = new ObjectTraits("not a date");
    traits.asTemporalAccessor(); // Should throw
  }

  // --- value() Test ---

  @Test
  public void ref_shouldReturnOriginalObject() {
    Object obj = new Object();
    ObjectTraits traits = new ObjectTraits(obj);
    assertSame(obj, traits.ref().get());
  }
}
