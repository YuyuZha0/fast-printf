package io.fastprintf;

import io.fastprintf.traits.*;
import io.fastprintf.util.Preconditions;

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

  private Args addTraits(FormatTraits traits) {
    this.traits.add(traits);
    return this;
  }

  @Override
  public List<Object> values() {
    List<Object> values = new ArrayList<>(traits.size());
    for (FormatTraits trait : traits) {
      values.add(trait.value());
    }
    return values;
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
  public Args putDateTime(TemporalAccessor value) {
    Preconditions.checkNotNull(value, "value");
    return addTraits(new TemporalAccessorTraits(value));
  }

  @Override
  public Iterator<FormatTraits> iterator() {
    return traits.iterator();
  }

  private Args putRaw(Object value) {
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

  @Override
  public Args put(Object value) {
    if (value == null) {
      return putNull();
    } else if (value instanceof Boolean) {
      return putBoolean((Boolean) value);
    } else if (value instanceof Character) {
      return putChar((Character) value);
    } else if (value instanceof Byte) {
      return putByte((Byte) value);
    } else if (value instanceof Short) {
      return putShort((Short) value);
    } else if (value instanceof Integer) {
      return putInt((Integer) value);
    } else if (value instanceof Long) {
      return putLong((Long) value);
    } else if (value instanceof Float) {
      return putFloat((Float) value);
    } else if (value instanceof Double) {
      return putDouble((Double) value);
    } else if (value instanceof CharSequence) {
      return putCharSequence((CharSequence) value);
    } else if (value instanceof BigInteger) {
      return putBigInteger((BigInteger) value);
    } else if (value instanceof BigDecimal) {
      return putBigDecimal((BigDecimal) value);
    } else if (value instanceof TemporalAccessor) {
      return putDateTime((TemporalAccessor) value);
    } else {
      return putRaw(value);
    }
  }
}
