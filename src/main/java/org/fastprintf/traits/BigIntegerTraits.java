package org.fastprintf.traits;

import org.fastprintf.box.FloatFamily;
import org.fastprintf.box.IntFamily;

import java.math.BigInteger;

public final class BigIntegerTraits implements FormatTraits {

  private final BigInteger value;

  public BigIntegerTraits(BigInteger value) {
    this.value = value;
  }

  @Override
  public IntFamily asIntFamily() {
    return IntFamily.valueOf(value);
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
