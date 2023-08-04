package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

import java.math.BigDecimal;

public final class BigDecimalTraits implements FormatTraits {

  private final BigDecimal value;

  public BigDecimalTraits(BigDecimal value) {
    this.value = value;
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(value.toBigInteger());
  }

  @Override
  public FloatFamily asFloatFamily() {
    return FloatFamily.valueOf(value);
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
