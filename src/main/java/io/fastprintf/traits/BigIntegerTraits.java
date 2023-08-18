package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.math.BigDecimal;
import java.math.BigInteger;

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
  public Object value() {
    return value;
  }
}