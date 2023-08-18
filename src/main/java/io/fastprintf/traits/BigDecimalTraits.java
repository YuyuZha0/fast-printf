package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;

import java.math.BigDecimal;

public final class BigDecimalTraits implements FormatTraits {

  private final BigDecimal value;

  public BigDecimalTraits(BigDecimal value) {
    this.value = value;
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(value.toBigInteger());
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(value);
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
