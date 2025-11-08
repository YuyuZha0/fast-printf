package io.fastprintf.seq;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An n-ary composite {@link Seq} that groups a sequence of {@link AtomicSeq} instances into a
 * single unit.
 *
 * <p>This class serves as an internal "composite" node in the rope-like data structure. Unlike
 * {@link Concat}, which is a binary node for pairwise appends, {@code SeqArray} is optimized for
 * joining a pre-existing list of segments all at once. It provides a flat, array-backed structure
 * that is more memory-efficient and faster to iterate over than a deep tree of {@code Concat}
 * nodes.
 *
 * <p>This class is an internal implementation detail and is primarily created by the {@link
 * Seq#join(java.util.List)} factory method. It is not intended for direct instantiation.
 *
 * @see Seq
 * @see AtomicSeq
 * @see Concat
 */
final class SeqArray implements AtomicSeqIterable {
  private final AtomicSeq[] array;
  private final int length;

  /**
   * Constructs a new SeqArray.
   *
   * @param array The array of atomic sequences. The array is used by reference and is not copied.
   * @param length The pre-calculated total length of all sequences in the array.
   */
  SeqArray(AtomicSeq[] array, int length) {
    this.array = array;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a new {@code SeqArray} where each contained {@code AtomicSeq} has been converted to
   * its uppercase equivalent. This operation does not modify the original sequence.
   */
  @Override
  public Seq upperCase() {
    AtomicSeq[] upperArray = new AtomicSeq[array.length];
    for (int i = 0; i < array.length; i++) {
      upperArray[i] = array[i].upperCase();
    }
    return new SeqArray(upperArray, length);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns a simple, efficient iterator that traverses the internal array of atomic sequences.
   */
  @Override
  public Iterator<AtomicSeq> iterator() {
    return new Itr(this.array);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(length);
    // The default appendTo implementation for AtomicSeqIterable is efficient here.
    appendTo(sb);
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   *
   * <p>For {@code SeqArray}, this method pushes child sequences from index {@code n-1} down to
   * {@code 1} onto the stack and returns the child at index {@code 0} for immediate processing by
   * the trampoline-style iterator.
   */
  @Override
  public Seq unfold(Deque<Seq> stack) {
    // Push all but the first element onto the stack in reverse order.
    for (int i = array.length - 1; i >= 1; i--) {
      stack.push(array[i]);
    }
    // Return the first element for immediate processing.
    return array[0];
  }

  /** A simple, efficient iterator over the internal array of atomic sequences. */
  private static final class Itr implements Iterator<AtomicSeq> {
    private final AtomicSeq[] array;
    private int index = 0;

    Itr(AtomicSeq[] array) {
      this.array = array;
    }

    @Override
    public boolean hasNext() {
      return index < array.length;
    }

    @Override
    public AtomicSeq next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return array[index++];
    }
  }
}
