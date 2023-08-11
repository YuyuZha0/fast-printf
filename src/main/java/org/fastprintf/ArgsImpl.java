package org.fastprintf;

import org.fastprintf.traits.BigDecimalTraits;
import org.fastprintf.traits.BigIntegerTraits;
import org.fastprintf.traits.BooleanTraits;
import org.fastprintf.traits.ByteTraits;
import org.fastprintf.traits.CharSequenceTraits;
import org.fastprintf.traits.CharacterTraits;
import org.fastprintf.traits.DoubleTraits;
import org.fastprintf.traits.FloatTraits;
import org.fastprintf.traits.FormatTraits;
import org.fastprintf.traits.IntTraits;
import org.fastprintf.traits.LongTraits;
import org.fastprintf.traits.NullTraits;
import org.fastprintf.traits.ObjectTraits;
import org.fastprintf.traits.ShortTraits;
import org.fastprintf.util.Preconditions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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

  private Args addTraits(FormatTraits traits) {
    this.traits.add(traits);
    return this;
  }

  @Override
  public List<FormatTraits> asList() {
    return Collections.unmodifiableList(traits);
  }

  @Override
  public Args putNull() {
    return addTraits(NullTraits.getInstance());
  }

  @Override
  public Args putBoolean(boolean value) {
    return addTraits(BooleanTraits.valueOf(value));
  }

  @Override
  public Args putChar(char value) {
    return addTraits(new CharacterTraits(value));
  }

  @Override
  public Args putByte(byte value) {
    return addTraits(new ByteTraits(value));
  }

  @Override
  public Args putShort(short value) {
    return addTraits(new ShortTraits(value));
  }

  @Override
  public Args putInt(int value) {
    return addTraits(new IntTraits(value));
  }

  @Override
  public Args putLong(long value) {
    return addTraits(new LongTraits(value));
  }

  @Override
  public Args putFloat(float value) {
    return addTraits(new FloatTraits(value));
  }

  @Override
  public Args putDouble(double value) {
    return addTraits(new DoubleTraits(value));
  }

  @Override
  public Args putCharSequence(CharSequence value) {
    Preconditions.checkNotNull(value, "value");
    return addTraits(new CharSequenceTraits(value));
  }

  @Override
  public Args putBigInteger(BigInteger value) {
    Preconditions.checkNotNull(value, "value");
    return addTraits(new BigIntegerTraits(value));
  }

  @Override
  public Args putBigDecimal(BigDecimal value) {
    Preconditions.checkNotNull(value, "value");
    return addTraits(new BigDecimalTraits(value));
  }

  @Override
  public Iterator<FormatTraits> iterator() {
    return traits.iterator();
  }

  @Override
  public Args putRaw(Object value) {
    if (value instanceof FormatTraits) {
      return addTraits((FormatTraits) value);
    }
    Preconditions.checkNotNull(value, "value");
    return addTraits(new ObjectTraits(value));
  }

  @Override
  public String toString() {
    return traits.stream()
        .map(FormatTraits::value)
        .map(String::valueOf)
        .collect(Collectors.joining(", ", "[", "]"));
  }
}
