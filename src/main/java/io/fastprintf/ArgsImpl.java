package io.fastprintf;

import io.fastprintf.traits.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

final class ArgsImpl implements Args {

  private final List<FormatTraits> traits;

  ArgsImpl(List<FormatTraits> traits) {
    this.traits = traits;
  }

  ArgsImpl() {
    this(new ArrayList<>());
  }

  ArgsImpl(int size) {
    this(new ArrayList<>(size));
  }

  private ArgsImpl addTraits(FormatTraits traits) {
    this.traits.add(traits);
    return this;
  }

  @Override
  public List<Object> values() {
    List<Object> values = new ArrayList<>(traits.size());
    for (FormatTraits trait : traits) {
      values.add(trait.asObject());
    }
    return values;
  }

  @Override
  public ArgsImpl putNull() {
    return addTraits(NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putBoolean(boolean value) {
    return addTraits(BooleanTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putBooleanOrNull(Boolean value) {
    return addTraits(
        value != null ? new BooleanTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putChar(char value) {
    return addTraits(CharacterTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putCharOrNull(Character value) {
    return addTraits(
        value != null ? new CharacterTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putByte(byte value) {
    return addTraits(ByteTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putByteOrNull(Byte value) {
    return addTraits(
        value != null ? new ByteTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putShort(short value) {
    return addTraits(ShortTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putShortOrNull(Short value) {
    return addTraits(
        value != null ? new ShortTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putInt(int value) {
    return addTraits(IntTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putIntOrNull(Integer value) {
    return addTraits(
        value != null ? new IntTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putLong(long value) {
    return addTraits(LongTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putLongOrNull(Long value) {
    return addTraits(
        value != null ? new LongTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putFloat(float value) {
    return addTraits(FloatTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putFloatOrNull(Float value) {
    return addTraits(
        value != null ? new FloatTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putDouble(double value) {
    return addTraits(DoubleTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putDoubleOrNull(Double value) {
    return addTraits(
        value != null ? new DoubleTraits(value, RefSlot.of(value)) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putCharSequence(CharSequence value) {
    return addTraits(value != null ? new CharSequenceTraits(value) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putBigInteger(BigInteger value) {
    return addTraits(value != null ? new BigIntegerTraits(value) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putBigDecimal(BigDecimal value) {
    return addTraits(value != null ? new BigDecimalTraits(value) : NullTraits.getInstance());
  }

  @Override
  public ArgsImpl putDateTime(TemporalAccessor value) {
    return addTraits(value != null ? new TemporalAccessorTraits(value) : NullTraits.getInstance());
  }

  @Override
  public Iterator<FormatTraits> iterator() {
    return traits.iterator();
  }

  @Override
  public String toString() {
    return traits.stream().map(FormatTraits::asString).collect(Collectors.joining(", ", "[", "]"));
  }

  @Override
  public Args put(Object value) {
    return addTraits(FormatTraits.convert(value));
  }

  @Override
  public ArgsImpl putString(String value) {
    return putCharSequence(value);
  }
}
