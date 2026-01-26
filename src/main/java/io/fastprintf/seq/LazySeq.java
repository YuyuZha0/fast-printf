package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * An atomic sequence that lazily evaluates its content to enable zero-copy appending.
 *
 * <h2>Core Concept</h2>
 *
 * <p>This class is a key component of the library's performance model for non-string arguments
 * (like numbers). Instead of immediately creating a {@code String} for a formatted number, it wraps
 * the logic required to generate that string (an {@code action}) along with its known,
 * pre-calculated {@code length}.
 *
 * <p>The actual string creation is deferred until it's absolutely necessary.
 *
 * <h2>Performance Model: Fast Path vs. Materialization</h2>
 *
 * <p><b>1. The Fast Path (Zero-Copy):</b> The primary optimization is the {@link
 * #appendTo(StringBuilder)} method. When a {@code LazySeq} is appended to the final {@code
 * StringBuilder}, it executes its {@code action} directly on that builder. This writes the
 * formatted content straight into the destination buffer, completely avoiding the allocation of any
 * intermediate {@code String} or {@code char[]} objects.
 *
 * <p><b>2. Materialization and Caching (Slow Path):</b> If any other method is called (e.g., {@link
 * #toString()}, {@link #charAt(int)}), the sequence must be "materialized." The lazy action is
 * executed, a new {@code String} is created, and that string is then **cached internally**. All
 * subsequent calls to these methods will use the cached string, making them fast after the initial
 * one-time cost.
 *
 * <p>While the internal cache is mutated on first use, the class is effectively immutable from the
 * caller's perspective, as the generated content never changes. This class is an internal
 * implementation detail, primarily created via the {@link Seq#lazy(Consumer, int)} factory method.
 *
 * @see Seq
 * @see AtomicSeq
 */
final class LazySeq implements AtomicSeq {

  /** The deferred string-building logic. */
  private final Consumer<? super StringBuilder> action;

  /** The pre-calculated, exact length of the sequence that the action will produce. */
  private final int length;

  /**
   * The cached string representation, lazily initialized. It remains {@code null} until first
   * access.
   */
  private String str;

  /**
   * Constructs a new LazySeq.
   *
   * @param action the deferred logic to generate the sequence's content.
   * @param length the exact, known length of the content that will be generated.
   */
  LazySeq(Consumer<? super StringBuilder> action, int length) {
    this.action = action;
    this.length = length;
  }

  /**
   * A static helper to execute a lazy action immediately and validate its output length.
   *
   * @param action the action to execute.
   * @param length the expected length of the output.
   * @return a {@code StringBuilder} containing the result.
   * @throws IllegalStateException if the actual length produced by the action does not match the
   *     expected length.
   */
  static StringBuilder buildEagerly(Consumer<? super StringBuilder> action, int length) {
    StringBuilder sb = new StringBuilder(length);
    action.accept(sb);
    if (length != sb.length()) {
      throw new IllegalStateException(
          "Length mismatch: expected " + length + " but got " + sb.length());
    }
    return sb;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation materializes and caches the underlying string on its first call and returns a
   * new view of that string.
   */
  @Override
  public AtomicSeq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    String value = getCachedString();
    return Seq.wrap(value, start, end);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation materializes and caches the underlying string on its first call, then returns
   * a new sequence representing its uppercase version.
   */
  @Override
  public AtomicSeq upperCase() {
    return Seq.wrap(getCachedString()).upperCase();
  }

  @Override
  public int length() {
    return length;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation materializes and caches the underlying string on its first call.
   */
  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, length);
    String value = getCachedString();
    return value.charAt(index);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation materializes and caches the underlying string on its first call. Subsequent
   * calls return the cached instance.
   */
  @Override
  public String toString() {
    return getCachedString();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation materializes and caches the underlying string on its first call, then appends
   * it to the given {@link Appendable}.
   */
  @Override
  public void appendTo(Appendable appendable) throws IOException {
    appendable.append(getCachedString());
  }

  /**
   * Executes the deferred action directly on the target {@code StringBuilder}.
   *
   * <p>This is the high-performance "fast path" for this class. It avoids creating any intermediate
   * string representation by writing the content directly into the destination buffer.
   *
   * @param sb the {@code StringBuilder} to which the content will be appended.
   */
  @Override
  public void appendTo(StringBuilder sb) {
    sb.ensureCapacity(sb.length() + length);
    action.accept(sb);
  }

  /**
   * Retrieves the string representation of this sequence, lazily generating and caching it on the
   * first call.
   *
   * @return the materialized {@code String}.
   */
  private String getCachedString() {
    String str = this.str;
    if (str == null) {
      str = buildEagerly(this.action, this.length).toString();
      this.str = str;
    }
    return str;
  }
}
