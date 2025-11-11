package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An internal "composite" node in the {@code Seq} rope-like data structure.
 *
 * <p>This interface represents a {@code Seq} that is composed of other {@code Seq} instances and
 * can be iterated to yield its constituent {@link AtomicSeq} leaf nodes. It serves as the base for
 * classes like {@link Concat} and {@link SeqArray}.
 *
 * <p>It provides default implementations for many {@code Seq} methods (e.g., {@code charAt}, {@code
 * subSequence}). These implementations work by iterating over the child {@code AtomicSeq} parts,
 * providing correct and consistent behavior for any composite sequence. Implementations of this
 * interface are only required to provide the {@link #unfold} method, an {@link #iterator}, and a
 * {@link #length} implementation.
 */
public interface AtomicSeqIterable extends Seq, Iterable<AtomicSeq> {

  /**
   * {@inheritDoc}
   *
   * <p>This implementation works by iterating through the child {@code AtomicSeq} parts to find the
   * correct segment and then creating a new composite {@code Seq} from the required subsequences of
   * those children.
   */
  @Override
  default Seq subSequence(int start, int end) {
    int length = length();
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) return Seq.empty();
    if (start == 0 && end == length) return this;
    List<AtomicSeq> buffer = new ArrayList<>();
    for (AtomicSeq seq : this) {
      int seqLength = seq.length();
      if (start < seqLength) {
        if (end <= seqLength) {
          buffer.add(seq.subSequence(start, end));
          break;
        }
        buffer.add(seq.subSequence(start, seqLength));
        start = 0;
      } else {
        start -= seqLength;
      }
      end -= seqLength;
    }
    assert !buffer.isEmpty();
    if (buffer.size() == 1) {
      return buffer.get(0);
    }
    return new SeqArray(buffer.toArray(new AtomicSeq[0]), end - start);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation works by iterating through the child {@code AtomicSeq} parts to locate
   * the one containing the requested index.
   */
  @Override
  default char charAt(int index) {
    Preconditions.checkPositionIndex(index, length());
    for (AtomicSeq seq : this) {
      if (index < seq.length()) {
        return seq.charAt(index);
      }
      index -= seq.length();
    }
    throw new AssertionError("Unreachable"); // Should be unreachable
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation efficiently appends the contents by iterating through its child {@code
   * AtomicSeq} parts and calling {@code appendTo} on each one.
   */
  @Override
  default void appendTo(Appendable appendable) throws IOException {
    for (AtomicSeq seq : this) {
      seq.appendTo(appendable);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation efficiently appends the contents by iterating through its child {@code
   * AtomicSeq} parts and calling {@code appendTo} on each one.
   */
  @Override
  default void appendTo(StringBuilder sb) {
    sb.ensureCapacity(sb.length() + length());
    for (AtomicSeq seq : this) {
      seq.appendTo(sb);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation finds the character by searching through each child {@code AtomicSeq} in
   * order, adjusting the returned index by the cumulative length of the preceding parts.
   */
  @Override
  default int indexOf(char c) {
    int currentLength = 0;
    for (AtomicSeq seq : this) {
      int index = seq.indexOf(c);
      if (index != INDEX_NOT_FOUND) {
        return index + currentLength;
      }
      currentLength += seq.length();
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation creates a new composite sequence by calling {@code upperCase()} on each
   * child {@code AtomicSeq} and joining the results.
   */
  @Override
  default Seq upperCase() {
    List<AtomicSeq> buffer = new ArrayList<>(elementCount());
    for (AtomicSeq seq : this) {
      buffer.add(seq.upperCase());
    }
    return new SeqArray(buffer.toArray(new AtomicSeq[0]), length());
  }

  /**
   * Unfolds a composite node for a trampoline-style iterator. This method returns the first child
   * for immediate processing and pushes the remaining children onto the given stack for later
   * traversal.
   *
   * @param stack The traversal stack.
   * @return The first child sequence to be processed next in the traversal loop.
   */
  Seq unfold(Deque<Seq> stack);

  @Override
  default boolean isAtomic() {
    return false;
  }
}
