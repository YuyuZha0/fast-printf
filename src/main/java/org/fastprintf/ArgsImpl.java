package org.fastprintf;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class ArgsImpl implements Args {

  private static final FormatTraits NULL = new NullTraits();
  private static final FormatTraits TRUE = new BooleanTraits(true);
  private static final FormatTraits FALSE = new BooleanTraits(false);

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
    return addTraits(NULL);
  }

  @Override
  public Args putBoolean(boolean value) {
    return addTraits(value ? TRUE : FALSE);
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
    Objects.requireNonNull(value, "value is null");
    return addTraits(new CharSequenceTraits(value));
  }

  @Override
  public Args putBigInteger(BigInteger value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Args putBigDecimal(BigDecimal value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<FormatTraits> iterator() {
    return traits.iterator();
  }

  @Override
  public Args putAny(Object value) {
    if (value instanceof FormatTraits) {
      return addTraits((FormatTraits) value);
    }
    Objects.requireNonNull(value, "value is null");
    return addTraits(new ObjectTraits(value));
  }
}
