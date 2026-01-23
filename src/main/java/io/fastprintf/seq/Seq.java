package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A rope-like, zero-copy character sequence for efficient string building.
 *
 * <p>This interface is the base for a data structure that allows for fast concatenation of
 * character sequences without immediate data copying. It forms a tree where leaf nodes ({@link
 * AtomicSeq}) hold character data and internal nodes ({@link AtomicSeqIterable}) represent
 * compositions.
 *
 * <p>The primary benefit of this structure is that operations like {@link #append(Seq)} and {@link
 * #concat(Seq, Seq)} are typically O(1) in time complexity, as they only involve creating a new
 * lightweight composite object. The final, flat string is only rendered when methods like {@link
 * #toString()} or {@link #appendTo(StringBuilder)} are called, at which point the entire tree is
 * traversed in a single, efficient pass.
 *
 * <p>Implementations are immutable; methods that modify the sequence, such as {@code append},
 * return a new {@code Seq} instance.
 */
public interface Seq extends CharSequence {

  /** Constant returned by {@link #indexOf(char)} when the character is not found. */
  int INDEX_NOT_FOUND = -1;

  /**
   * A performance-tuning constant used by {@link AtomicSeq} implementations to optimize {@code
   * appendTo(StringBuilder)} operations.
   *
   * <p>When appending a sequence of characters to a {@link StringBuilder}, there are two primary
   * strategies:
   *
   * <ol>
   *   <li><b>Looping:</b> Appending character-by-character in a simple {@code for} loop. This
   *       avoids any intermediate object allocation.
   *   <li><b>Array-based:</b> Allocating a temporary {@code char[]} array, filling it, and calling
   *       {@code StringBuilder.append(char[])} once.
   * </ol>
   *
   * <p>There is a significant performance trade-off between these two approaches. The looping
   * strategy is faster for a very small number of characters because it avoids the overhead of
   * array allocation. The array-based strategy is significantly faster for larger numbers of
   * characters because it leverages powerful JVM intrinsics (like {@code Arrays.fill} and {@code
   * System.arraycopy} used by {@code append(char[])}), which the JIT compiler can often convert to
   * highly optimized SIMD instructions.
   *
   * <p>The value of 16 represents an empirically determined **crossover point** based on JMH
   * microbenchmarks. Below this threshold, the loop is generally faster; above this threshold, the
   * array-based method provides a substantial performance gain that scales with the sequence
   * length.
   *
   * <p>This constant is intended as a guideline for implementers of the {@code Seq} interface, such
   * as {@link StrView} and {@link Repeated}, to make an optimal, data-driven choice between the two
   * appending strategies.
   *
   * <h2>Usage Example:</h2>
   *
   * <pre>{@code
   * @Override
   * public void appendTo(StringBuilder sb) {
   *     if (length < ARRAY_APPEND_THRESHOLD) {
   *         // For short sequences, the loop is faster.
   *         for (int i = 0; i < length; i++) {
   *             sb.append(this.charAt(i));
   *         }
   *     } else {
   *         // For longer sequences, the array path is faster.
   *         char[] chars = toCharArray();
   *         sb.append(chars);
   *     }
   * }
   * }</pre>
   *
   * @see AtomicSeq#appendTo(StringBuilder)
   * @see StrView#appendTo(StringBuilder)
   * @see Repeated#appendTo(StringBuilder)
   */
  int ARRAY_APPEND_THRESHOLD = 16;

  /**
   * Creates an atomic sequence containing a single character.
   *
   * @param c the character.
   * @return a new {@code AtomicSeq} instance.
   */
  static AtomicSeq ch(char c) {
    return Repeated.ofSingleChar(c);
  }

  /**
   * Creates an atomic sequence containing a character repeated a specified number of times.
   *
   * @param c the character to repeat.
   * @param count the number of times to repeat the character (must be &ge; 1).
   * @return a new {@code AtomicSeq} instance.
   */
  static AtomicSeq repeated(char c, int count) {
    Preconditions.checkArgument(count >= 1, "count < 1");
    if (count == 1) return ch(c);
    return new Repeated(c, count);
  }

  /**
   * Creates an atomic sequence that is a view of the given string.
   *
   * <p>This is a zero-copy operation; the sequence holds a reference to the original string.
   *
   * @param s the string to wrap.
   * @return a new {@code AtomicSeq} instance, or an empty sequence if the string is empty.
   */
  static AtomicSeq wrap(String s) {
    Preconditions.checkNotNull(s, "s");
    int length = s.length();
    if (length > 0) {
      if (length == 1) return ch(s.charAt(0));
      return new StrView(s, 0, length);
    }
    return empty();
  }

  /**
   * Creates an atomic sequence that is a view of a substring, from {@code start} to the end.
   *
   * @param s the source string.
   * @param start the starting index (inclusive).
   * @return a new {@code AtomicSeq} instance.
   */
  static AtomicSeq wrap(String s, int start) {
    return wrap(s, start, s.length());
  }

  /**
   * Creates an atomic sequence that is a view of a substring.
   *
   * @param s the source string.
   * @param start the starting index (inclusive).
   * @param end the ending index (exclusive).
   * @return a new {@code AtomicSeq} instance.
   */
  static AtomicSeq wrap(String s, int start, int end) {
    Preconditions.checkNotNull(s, "s");
    int length = s.length();
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) return empty();
    if (end == start + 1) return ch(s.charAt(start));
    return new StrView(s, start, end - start);
  }

  /**
   * Creates an atomic sequence that is a view of a character array sub-region.
   *
   * <p>This is a zero-copy operation; the sequence holds a reference to the original array.
   *
   * @param ch the source character array.
   * @param start the starting index.
   * @param length the number of characters to include.
   * @return a new {@code AtomicSeq} instance.
   */
  static AtomicSeq forArray(char[] ch, int start, int length) {
    Preconditions.checkNotNull(ch, "ch");
    Preconditions.checkPositionIndexes(start, start + length, ch.length);
    return CharArray.wrap(ch, start, length);
  }

  /**
   * Creates an atomic sequence that is a view of the given character array.
   *
   * <p>This is a zero-copy operation; the sequence holds a reference to the original array.
   *
   * @param ch the character array to wrap.
   * @return a new {@code AtomicSeq} instance.
   */
  static AtomicSeq forArray(char[] ch) {
    Preconditions.checkNotNull(ch, "ch");
    return CharArray.wrap(ch, 0, ch.length);
  }

  /**
   * Creates an atomic sequence backed by a builder action.
   *
   * <p>This method is intended for high-performance internal formatting where the length of the
   * output is known in advance (e.g., formatting an integer), allowing the data to be written
   * directly to the destination buffer without intermediate allocations.
   *
   * <p><b>Optimizations:</b>
   *
   * <ul>
   *   <li>If {@code length} is 0, returns {@link #empty()}.
   *   <li>If {@code length} is 1, the action is executed <b>immediately</b> to extract the
   *       character, and a cached single-character sequence is returned.
   *   <li>Otherwise, a lazy wrapper is returned, and the action is executed only when appended.
   * </ul>
   *
   * @param action the action to populate a {@link StringBuilder}.
   * @param length the exact length of the content produced by the action.
   * @return a new {@code AtomicSeq}.
   * @throws IllegalArgumentException if {@code length} is negative.
   * @throws IllegalStateException if {@code length} is 1 but the action does not produce exactly 1
   *     character.
   */
  static AtomicSeq lazy(Consumer<? super StringBuilder> action, int length) {
    Preconditions.checkNotNull(action, "action");
    Preconditions.checkArgument(length >= 0, "length < 0");
    if (length == 0) {
      return empty();
    } else if (length == 1) {
      // Eagerly build to leverage the cached Repeated instances for single chars.
      // This trades immediate execution for long-term memory savings and zero-object-overhead.
      return ch(LazySeq.buildEagerly(action, length).charAt(0));
    }
    return new LazySeq(action, length);
  }

  /**
   * Creates a composite sequence by concatenating two sequences.
   *
   * <p>This is typically an O(1) operation that creates a new {@link Concat} node.
   *
   * @param left the first (left) sequence.
   * @param right the second (right) sequence.
   * @return a new composite {@code Seq} instance.
   */
  static Seq concat(Seq left, Seq right) {
    Preconditions.checkNotNull(left, "left");
    Preconditions.checkNotNull(right, "right");
    return Concat.concat(left, right);
  }

  /**
   * Returns a singleton, empty atomic sequence.
   *
   * @return the empty {@code AtomicSeq} instance.
   */
  static AtomicSeq empty() {
    return EmptySeq.INSTANCE;
  }

  /**
   * Creates a composite sequence by joining a list of atomic sequences.
   *
   * <p>This is an efficient way to combine multiple pre-existing parts, typically creating a single
   * {@link SeqArray} object.
   *
   * @param atomicSeqs the list of atomic sequences to join.
   * @return a new composite {@code Seq}, or an empty sequence if the list is empty.
   */
  static Seq join(List<? extends AtomicSeq> atomicSeqs) {
    Preconditions.checkNotNull(atomicSeqs, "atomicSeqs");
    if (atomicSeqs.isEmpty()) {
      return empty();
    }
    if (atomicSeqs.size() == 1) {
      return atomicSeqs.get(0);
    }
    int totalLength = 0;
    for (AtomicSeq seq : atomicSeqs) {
      Preconditions.checkNotNull(seq, "seqElement");
      totalLength += seq.length();
    }

    return new SeqArray(atomicSeqs.toArray(new AtomicSeq[0]), totalLength);
  }

  /**
   * Returns a new sequence by prepending another sequence to this one.
   *
   * @param seq the sequence to prepend.
   * @return a new composite {@code Seq}, or this instance if {@code seq} is empty.
   */
  default Seq prepend(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(seq, this);
  }

  /**
   * Returns a new sequence by appending another sequence to this one.
   *
   * @param seq the sequence to append.
   * @return a new composite {@code Seq}, or this instance if {@code seq} is empty.
   */
  default Seq append(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(this, seq);
  }

  @Override
  Seq subSequence(int start, int end);

  /**
   * Appends the contents of this sequence to an {@link Appendable}.
   *
   * <p>This is a default, character-by-character implementation. Subclasses that can perform this
   * operation more efficiently (like by writing chunks) should override it.
   *
   * @param appendable the {@code Appendable} to write to.
   * @throws IOException if an I/O error occurs.
   */
  default void appendTo(Appendable appendable) throws IOException {
    int length = length();
    if (length == 0) return;
    for (int i = 0; i < length; i++) {
      appendable.append(charAt(i));
    }
  }

  /**
   * Appends the contents of this sequence to a {@link StringBuilder}.
   *
   * <p>This is a default, character-by-character implementation. Subclasses that can perform this
   * operation more efficiently should override it.
   *
   * @param sb the {@code StringBuilder} to write to.
   */
  default void appendTo(StringBuilder sb) {
    int length = length();
    if (length == 0) return;
    for (int i = 0; i < length; i++) {
      sb.append(charAt(i));
    }
  }

  /**
   * Returns this sequence. Useful for chaining in fluent APIs.
   *
   * @return this instance.
   */
  default Seq dup() {
    return this;
  }

  /**
   * Returns a new sequence containing the uppercase equivalent of this sequence.
   *
   * @return a new uppercase {@code Seq}.
   */
  Seq upperCase();

  /**
   * Applies a function to this sequence.
   *
   * @param mapper the function to apply.
   * @return the result of applying the mapper function to this sequence.
   */
  default Seq map(Function<? super Seq, ? extends Seq> mapper) {
    Preconditions.checkNotNull(mapper, "mapper");
    return mapper.apply(this);
  }

  /**
   * Returns {@code true} if this sequence has a length of 0.
   *
   * @return {@code true} if {@link #length()} is 0, otherwise {@code false}.
   */
  default boolean isEmpty() {
    return length() == 0;
  }

  /**
   * Returns the index within this sequence of the first occurrence of the specified character.
   *
   * @param c the character to search for.
   * @return the index of the first occurrence, or {@link #INDEX_NOT_FOUND} if not found.
   */
  default int indexOf(char c) {
    int length = length();
    for (int i = 0; i < length; i++) {
      if (charAt(i) == c) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * Tests if this sequence starts with the specified character.
   *
   * @param prefix the character to check.
   * @return {@code true} if this sequence is not empty and its first character is {@code prefix}.
   */
  default boolean startsWith(char prefix) {
    return length() > 0 && charAt(0) == prefix;
  }

  /**
   * Returns the total number of atomic (leaf) sequences within this composite sequence.
   *
   * <p>This method allows for O(1) retrieval of the number of leaf nodes, which can be used as a
   * heuristic for optimizations, such as pre-sizing collections or iterators.
   *
   * @return the number of leaf nodes.
   */
  int elementCount();

  /**
   * Returns {@code true} if this sequence is atomic (a leaf node), otherwise {@code false}.
   *
   * @return {@code true} if this is an {@link AtomicSeq}, otherwise {@code false}.
   */
  boolean isAtomic();
}
