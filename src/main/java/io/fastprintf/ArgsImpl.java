package io.fastprintf;

import io.fastprintf.traits.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
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

  private <T> ArgsImpl addTraits(
      T value, BiFunction<? super T, ? super RefSlot, ? extends FormatTraits> factory) {
    if (value == null) {
      return putNull();
    }
    return addTraits(factory.apply(value, RefSlot.of(value)));
  }

  private <T> ArgsImpl addTraits(T value, Function<? super T, ? extends FormatTraits> factory) {
    if (value == null) {
      return putNull();
    }
    return addTraits(factory.apply(value));
  }

  private <T> ArgsImpl addTraitsNonNull(
      T value, BiFunction<? super T, ? super RefSlot, ? extends FormatTraits> factory) {
    return addTraits(factory.apply(value, RefSlot.of(value)));
  }

  private <T> ArgsImpl addTraitsNonNull(
      T value, Function<? super T, ? extends FormatTraits> factory) {
    return addTraits(factory.apply(value));
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
    return addTraits(value, BooleanTraits::new);
  }

  @Override
  public ArgsImpl putChar(char value) {
    return addTraits(CharacterTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putCharOrNull(Character value) {
    return addTraits(value, CharacterTraits::new);
  }

  @Override
  public ArgsImpl putByte(byte value) {
    return addTraits(ByteTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putByteOrNull(Byte value) {
    return addTraits(value, ByteTraits::new);
  }

  @Override
  public ArgsImpl putShort(short value) {
    return addTraits(ShortTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putShortOrNull(Short value) {
    return addTraits(value, ShortTraits::new);
  }

  @Override
  public ArgsImpl putInt(int value) {
    return addTraits(IntTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putIntOrNull(Integer value) {
    return addTraits(value, IntTraits::new);
  }

  @Override
  public ArgsImpl putLong(long value) {
    return addTraits(LongTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putLongOrNull(Long value) {
    return addTraits(value, LongTraits::new);
  }

  @Override
  public ArgsImpl putFloat(float value) {
    return addTraits(FloatTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putFloatOrNull(Float value) {
    return addTraits(value, FloatTraits::new);
  }

  @Override
  public ArgsImpl putDouble(double value) {
    return addTraits(DoubleTraits.ofPrimitive(value));
  }

  @Override
  public ArgsImpl putDoubleOrNull(Double value) {
    return addTraits(value, DoubleTraits::new);
  }

  @Override
  public ArgsImpl putCharSequence(CharSequence value) {
    return addTraits(value, CharSequenceTraits::new);
  }

  @Override
  public ArgsImpl putBigInteger(BigInteger value) {
    return addTraits(value, BigIntegerTraits::new);
  }

  @Override
  public ArgsImpl putBigDecimal(BigDecimal value) {
    return addTraits(value, BigDecimalTraits::new);
  }

  @Override
  public ArgsImpl putDateTime(TemporalAccessor value) {
    return addTraits(value, TemporalAccessorTraits::new);
  }

  @Override
  public Iterator<FormatTraits> iterator() {
    return traits.iterator();
  }

  private ArgsImpl putObject(Object value) {
    if (value instanceof FormatTraits) {
      return addTraits((FormatTraits) value);
    }
    return addTraits(value, ObjectTraits::new);
  }

  @Override
  public String toString() {
    return traits.stream().map(FormatTraits::asString).collect(Collectors.joining(", ", "[", "]"));
  }

  @Override
  public Args put(Object value) {
    if (value == null) {
      return putNull();
    } else if (value instanceof Boolean) {
      return addTraitsNonNull((Boolean) value, BooleanTraits::new);
    } else if (value instanceof Character) {
      return addTraitsNonNull((Character) value, CharacterTraits::new);
    } else if (value instanceof Byte) {
      return addTraitsNonNull((Byte) value, ByteTraits::new);
    } else if (value instanceof Short) {
      return addTraitsNonNull((Short) value, ShortTraits::new);
    } else if (value instanceof Integer) {
      return addTraitsNonNull((Integer) value, IntTraits::new);
    } else if (value instanceof Long) {
      return addTraitsNonNull((Long) value, LongTraits::new);
    } else if (value instanceof Float) {
      return addTraitsNonNull((Float) value, FloatTraits::new);
    } else if (value instanceof Double) {
      return addTraitsNonNull((Double) value, DoubleTraits::new);
    } else if (value instanceof CharSequence) {
      return addTraitsNonNull((CharSequence) value, CharSequenceTraits::new);
    } else if (value instanceof BigInteger) {
      return addTraitsNonNull((BigInteger) value, BigIntegerTraits::new);
    } else if (value instanceof BigDecimal) {
      return addTraitsNonNull((BigDecimal) value, BigDecimalTraits::new);
    } else if (value instanceof TemporalAccessor) {
      return addTraitsNonNull((TemporalAccessor) value, TemporalAccessorTraits::new);
    } else {
      return putObject(value);
    }
  }
}
