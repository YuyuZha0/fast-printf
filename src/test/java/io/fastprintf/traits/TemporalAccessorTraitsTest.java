package io.fastprintf.traits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import io.fastprintf.PrintfException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import org.junit.Test;

public class TemporalAccessorTraitsTest {

  // --- Tests for asFloatForm() ---

  @Test
  public void asFloatForm_withInstant_isCorrect() {
    Instant testInstant = Instant.parse("2023-11-15T10:30:45.123456789Z");
    System.out.println(testInstant.getEpochSecond());
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testInstant);

    // Expected value is based on the UTC instant
    double expectedSeconds = 1700044245.123456789;
    String expectedString = String.valueOf(expectedSeconds);

    String actualString = traits.asFloatForm().toString();

    assertEquals("FloatForm from Instant should be correct", expectedString, actualString);
  }

  @Test
  public void asFloatForm_withZonedDateTime_isCorrect() {
    Instant testInstant = Instant.parse("2023-11-15T10:30:45.123456789Z");
    ZonedDateTime testZdt = testInstant.atZone(ZoneId.of("America/New_York"));
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testZdt);

    // The expected value is STILL based on the underlying UTC instant, not the local time.
    double expectedSeconds = 1700044245.123456789;
    String expectedString = String.valueOf(expectedSeconds);

    String actualString = traits.asFloatForm().toString();

    // Add debug output to be absolutely sure what is being compared
    System.out.println("--- ZonedDateTime Test ---");
    System.out.println("Input ZonedDateTime: " + testZdt);
    System.out.println("Expected Value: " + expectedString);
    System.out.println("Actual Value:   " + actualString);
    System.out.println("--------------------------");

    assertEquals(
        "FloatForm from ZonedDateTime should be based on UTC Instant",
        expectedString,
        actualString);
  }

  @Test
  public void asFloatForm_withMillisOnlyInstant_isCorrect() {
    Instant testInstant = Instant.ofEpochMilli(1699987654321L);
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testInstant);

    double expectedSeconds = 1699987654.321;

    assertEquals(String.valueOf(expectedSeconds), traits.asFloatForm().toString());
  }

  // --- Tests for other methods ---

  @Test
  public void asIntForm_withInstant_isCorrect() {
    Instant testInstant = Instant.parse("2023-11-15T10:30:45.123Z");
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testInstant);
    long expectedMillis = testInstant.toEpochMilli();

    assertEquals(String.valueOf(expectedMillis), traits.asIntForm().toDecimalString());
  }

  @Test
  public void asString_withInstant_isCorrect() {
    Instant testInstant = Instant.parse("2023-11-15T10:30:45.123Z");
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testInstant);

    assertEquals(testInstant.toString(), traits.asString());
  }

  @Test
  public void asTemporalAccessor_withInstant_isCorrect() {
    Instant testInstant = Instant.now();
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testInstant);

    assertSame(testInstant, traits.asTemporalAccessor());
  }

  @Test
  public void asInt_withInstant_isCorrect() {
    Instant testInstant = Instant.parse("2023-11-15T10:30:45.123Z");
    TemporalAccessorTraits traits = new TemporalAccessorTraits(testInstant);

    assertEquals((int) testInstant.getEpochSecond(), traits.asInt());
  }

  @Test(expected = PrintfException.class)
  public void asChar_shouldThrowException() {
    TemporalAccessorTraits traits = new TemporalAccessorTraits(Instant.EPOCH);
    traits.asChar(); // This should throw
  }

  @Test
  public void conversions_forUnsupportedTypes_shouldThrowException() {
    TemporalAccessor localDateTime = LocalDateTime.now();
    TemporalAccessor localDate = LocalDate.now();

    TemporalAccessorTraits ldtTraits = new TemporalAccessorTraits(localDateTime);
    TemporalAccessorTraits ldTraits = new TemporalAccessorTraits(localDate);

    try {
      ldtTraits.asFloatForm();
      fail("LocalDateTime to FloatForm should have thrown PrintfException");
    } catch (PrintfException expected) {
      // Success
    }

    try {
      ldTraits.asIntForm();
      fail("LocalDate to IntForm should have thrown PrintfException");
    } catch (PrintfException expected) {
      // Success
    }
  }
}
