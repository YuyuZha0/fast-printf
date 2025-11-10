package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public final class TemporalAccessorTraits implements FormatTraits {

  private final TemporalAccessor value;

  public TemporalAccessorTraits(TemporalAccessor value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(toEpochMilli());
  }

  @Override
  public FloatForm asFloatForm() {
    try {
      // First, convert the accessor to a canonical Instant. This is the
      // most robust way to handle all types (ZonedDateTime, OffsetDateTime, etc.).
      Instant instant = Instant.from(value);

      // Then, derive the double value from the Instant's definitive epoch values.
      double seconds = instant.getEpochSecond();
      seconds += instant.getNano() / 1_000_000_000.0D;

      return FloatForm.valueOf(seconds);
    } catch (DateTimeException e) {
      // This will be caught if the TemporalAccessor cannot be converted to an Instant
      // (e.g., LocalDate, LocalDateTime), which is the desired failure behavior.
      throw new PrintfException("Can't get epoch seconds from: %s", value, e);
    }
  }

  @Override
  public String asString() {
    return value.toString();
  }

  private long toEpochMilli() {
    try {
      // The most robust way to get epoch milliseconds is also via Instant.
      return Instant.from(value).toEpochMilli();
    } catch (DateTimeException e) {
      throw new PrintfException("Can't get epoch milliseconds from: %s", value, e);
    }
  }

  private long getEpochSecond() {
    // This can now be simplified or even removed if not used elsewhere,
    // but for consistency with asInt(), we can keep it.
    try {
      return Instant.from(value).getEpochSecond();
    } catch (DateTimeException e) {
      throw new PrintfException("Can't get epoch seconds from: %s", value, e);
    }
  }

  @Override
  public int asInt() {
    return (int) getEpochSecond();
  }

  @Override
  public RefSlot ref() {
    return RefSlot.of(value);
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return value;
  }

  @Override
  public char asChar() {
    throw new PrintfException("Can't convert TemporalAccessor to char");
  }
}
