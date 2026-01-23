package io.fastprintf.traits;

import io.fastprintf.number.FloatForm;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import io.fastprintf.util.Utils;
import java.time.temporal.TemporalAccessor;

public final class LongTraits implements FormatTraits {

  private final long value;
  private final RefSlot ref;

  public LongTraits(long value, RefSlot ref) {
    this.value = value;
    this.ref = ref;
  }

  public static LongTraits ofPrimitive(long value) {
    return new LongTraits(value, RefSlot.ofPrimitive());
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
    return Long.toString(value);
  }

  @Override
  public int asInt() {
    return (int) value;
  }

  @Override
  public RefSlot ref() {
    return ref;
  }

  @Override
  public TemporalAccessor asTemporalAccessor() {
    return Utils.longToInstant(value);
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
