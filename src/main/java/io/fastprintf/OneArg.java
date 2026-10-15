package io.fastprintf;

import io.fastprintf.traits.FormatTraits;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Fixed single-argument storage without a growable list; each iteration has its own cursor. */
final class OneArg implements Args {

  private final FormatTraits trait;

  OneArg(FormatTraits trait) {
    this.trait = trait;
  }

  private static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("Cannot append to a single-argument container");
  }

  @Override
  public List<Object> values() {
    return Collections.singletonList(trait.asObject());
  }

  @Override
  public Args putNull() {
    throw unsupported();
  }

  @Override
  public Args putBoolean(boolean value) {
    throw unsupported();
  }

  @Override
  public Args putBooleanOrNull(Boolean value) {
    throw unsupported();
  }

  @Override
  public Args putChar(char value) {
    throw unsupported();
  }

  @Override
  public Args putCharOrNull(Character value) {
    throw unsupported();
  }

  @Override
  public Args putByte(byte value) {
    throw unsupported();
  }

  @Override
  public Args putByteOrNull(Byte value) {
    throw unsupported();
  }

  @Override
  public Args putShort(short value) {
    throw unsupported();
  }

  @Override
  public Args putShortOrNull(Short value) {
    throw unsupported();
  }

  @Override
  public Args putInt(int value) {
    throw unsupported();
  }

  @Override
  public Args putIntOrNull(Integer value) {
    throw unsupported();
  }

  @Override
  public Args putLong(long value) {
    throw unsupported();
  }

  @Override
  public Args putLongOrNull(Long value) {
    throw unsupported();
  }

  @Override
  public Args putFloat(float value) {
    throw unsupported();
  }

  @Override
  public Args putFloatOrNull(Float value) {
    throw unsupported();
  }

  @Override
  public Args putDouble(double value) {
    throw unsupported();
  }

  @Override
  public Args putDoubleOrNull(Double value) {
    throw unsupported();
  }

  @Override
  public Args putDateTime(TemporalAccessor value) {
    throw unsupported();
  }

  @Override
  public Args putCharSequence(CharSequence value) {
    throw unsupported();
  }

  @Override
  public Args putBigInteger(BigInteger value) {
    throw unsupported();
  }

  @Override
  public Args putBigDecimal(BigDecimal value) {
    throw unsupported();
  }

  @Override
  public Args put(Object value) {
    throw unsupported();
  }

  @Override
  public Iterator<FormatTraits> iterator() {
    // Independent cursors allow reuse and nested iteration without a shared mutable position.
    return new SingleIterator(trait);
  }

  @Override
  public String toString() {
    return "[" + trait.asString() + "]";
  }

  private static final class SingleIterator implements Iterator<FormatTraits> {
    private FormatTraits remaining;

    private SingleIterator(FormatTraits trait) {
      this.remaining = trait;
    }

    @Override
    public boolean hasNext() {
      return remaining != null;
    }

    @Override
    public FormatTraits next() {
      if (remaining == null) {
        throw new NoSuchElementException();
      }
      FormatTraits result = remaining;
      // A null argument is represented by NullTraits, so null can mark exhaustion here.
      remaining = null;
      return result;
    }
  }
}
