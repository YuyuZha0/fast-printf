package io.fastprintf.traits;

/**
 * A container that explicitly manages the object identity of a formatting argument.
 *
 * <p>This class distinguishes between three fundamental states for an argument:
 *
 * <ol>
 *   <li><b>Primitive:</b> The argument originated from a primitive type (e.g., {@code int}, {@code
 *       double}). Such values have no stable object identity.
 *   <li><b>Null Reference:</b> The argument was a {@code null} object reference.
 *   <li><b>Object Reference:</b> The argument was a non-null object, whose identity can be
 *       inspected (e.g., for the {@code %p} specifier).
 * </ol>
 *
 * <p>By making this distinction a first-class concept, this class allows the formatting engine to
 * handle object identity correctly and robustly, preventing bugs related to auto-boxing.
 *
 * @see FormatTraits#ref()
 */
public final class RefSlot {

  private static final Object PRIMITIVE_DEFAULT = new Object();

  private static final RefSlot PRIMITIVE = new RefSlot(PRIMITIVE_DEFAULT);
  private static final RefSlot NULL = new RefSlot(null);

  private final Object value;

  RefSlot(Object value) {
    this.value = value;
  }

  public static RefSlot ofPrimitive() {
    return PRIMITIVE;
  }

  public static RefSlot ofNull() {
    return NULL;
  }

  public static RefSlot of(Object value) {
    if (value == null) {
      return NULL;
    }
    return new RefSlot(value);
  }

  public boolean isPrimitive() {
    return this == PRIMITIVE || this.value == PRIMITIVE_DEFAULT;
  }

  public Object get() {
    if (isPrimitive()) {
      throw new UnsupportedOperationException("RefSlot is primitive");
    }
    return value;
  }
}
