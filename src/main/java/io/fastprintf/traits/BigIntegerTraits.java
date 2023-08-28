package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.util.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;

public final class BigIntegerTraits implements FormatTraits {

  private final BigInteger value;

  public BigIntegerTraits(BigInteger value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(value);
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(new BigDecimal(value, 0));
  }

  @Override
  public String asString() {
    return value.toString();
  }

  @Override
  public int asInt() {
    return value.intValue();
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return Utils.longToInstant(value.longValue());
  }

  @Override
  public Object value() {
    return value;
  }
}
