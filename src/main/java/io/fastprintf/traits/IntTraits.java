package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import io.fastprintf.util.Utils;
import java.time.temporal.TemporalAccessor;

public final class IntTraits implements FormatTraits {

  private static final int CACHE_LOW = -128;
  private static final int CACHE_HIGH = 127;
  private static final IntTraits[] CACHE;

  static {
    CACHE = new IntTraits[CACHE_HIGH - CACHE_LOW + 1];
    for (int i = 0; i < CACHE.length; i++) {
      CACHE[i] = new IntTraits(i + CACHE_LOW, RefSlot.ofPrimitive());
    }
  }

  private final int value;
  private final RefSlot ref;

  public IntTraits(int value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static IntTraits ofPrimitive(int value) {
    if (value >= CACHE_LOW && value <= CACHE_HIGH) {
      return CACHE[value - CACHE_LOW];
    }
    return new IntTraits(value, RefSlot.ofPrimitive());
  }

  @Override
  public IntForm asIntForm() {
    return IntForm.valueOf(value);
  }

  @Override
  public FloatForm asFloatForm() {
    return FloatForm.valueOf(value);
  }

  @Override
  public String asString() {
    return Integer.toString(value);
  }

  @Override
  public int asInt() {
    return value;
  }

  @Override
  public RefSlot ref() {
    return ref;
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return Utils.longToInstant(Integer.toUnsignedLong(value));
  }

  @Override
  public Object asObject() {
    return ref.isPrimitive() ? value : ref.get();
  }

  @Override
  public Seq asSeq() {
    return Seq.lazy(sb -> sb.append(value), Utils.stringSize(value));
  }
}
