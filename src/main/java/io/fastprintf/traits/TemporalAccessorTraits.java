package io.fastprintf.traits;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.time.temporal.ChronoField;
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
    double seconds = getEpochSecond();
    if (value.isSupported(ChronoField.NANO_OF_SECOND))
      seconds += value.getLong(ChronoField.NANO_OF_SECOND) / 1_000_000_000D;
    else if (value.isSupported(ChronoField.MILLI_OF_SECOND))
      seconds += value.getLong(ChronoField.MILLI_OF_SECOND) / 1_000D;
    return FloatForm.valueOf(seconds);
  }

  @Override
  public String asString() {
    return value.toString();
  }

  private long toEpochMilli() {
    long mills = getEpochSecond() * 1000L;
    if (value.isSupported(ChronoField.MILLI_OF_SECOND))
      mills += value.getLong(ChronoField.MILLI_OF_SECOND);
    return mills;
  }

  private long getEpochSecond() {
    if (!value.isSupported(ChronoField.INSTANT_SECONDS))
      throw new PrintfException("Can't get seconds from: %s", value);
    return value.getLong(ChronoField.INSTANT_SECONDS);
  }

  @Override
  public int asInt() {
    return (int) getEpochSecond();
  }

  @Override
  public Object value() {
    return value;
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
