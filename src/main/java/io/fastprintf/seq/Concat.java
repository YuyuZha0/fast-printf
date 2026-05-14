package io.fastprintf.seq;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A composite {@link Seq} representing the concatenation of two or more sequences, implemented as a
 * rope data structure.
 *
 * <p>This class is the core of the library's zero-copy string construction mechanism. Instead of
 * immediately merging character arrays upon concatenation, this class creates a lightweight,
 * immutable object that holds references to the left and right sequences. This forms a binary tree
 * where the internal nodes are {@code Concat} instances and the leaf nodes are {@link AtomicSeq}
 * instances that hold the actual character data.
 *
 * <h2>Architectural Benefits:</h2>
 *
 * <ul>
 *   <li><b>Fast Concatenation</b>: Appending sequences is an O(1) operation, as it only involves
 *       the creation of a new node in the tree, with no character data being copied.
 *   <li><b>Low Memory Overhead</b>: It avoids the creation of intermediate string objects and the
 *       costly resizing of buffers (as seen in {@link StringBuilder}), minimizing GC pressure.
 *   <li><b>Lazy Rendering</b>: The final, flat string is only rendered when required (e.g., by
 *       {@link #appendTo(StringBuilder)} or {@link #toString()}), at which point the tree is
 *       traversed efficiently in a single pass.
 * </ul>
 *
 * <p>The internal tree is automatically rebalanced to be right-leaning. This prevents deep
 * recursion and potential {@link StackOverflowError} when iterating over a sequence built from a
 * very long chain of appends.
 *
 * <p>This class is intended for internal use by the {@link Seq} factory methods and is not meant to
 * be instantiated directly.
 *
 * @see Seq
 * @see AtomicSeq
 */
final class Concat implements AtomicSeqIterable {

  private final Seq left;
  private final Seq right;
  private final int length;
  private final int elementCount;

  private Concat(Seq left, Seq right, int length, int elementCount) {
    this.left = left;
    this.right = right;
    this.length = length;
    this.elementCount = elementCount;
  }

  /**
   * Factory method to create a new {@code Concat} sequence.
   *
   * <p>This method includes a rebalancing optimization. To prevent the formation of a deep,
   * left-leaning tree (which is inefficient to traverse), it restructures {@code (a + b) + c} into
   * {@code a + (b + c)}. This ensures the tree remains relatively shallow and right-leaning, which
   * is optimal for the stack-based {@link ConcatIterator}.
   *
   * @param left the left sequence.
   * @param right the right sequence.
   * @return a new {@code Concat} instance.
   */
  static Concat concat(Seq left, Seq right) {
    if (left instanceof Concat) {
      // let the tree grow to the right, so the deque stack max size could be smaller
      return growToTheRight((Concat) left, right);
    } else {
      return concat0(left, right);
    }
  }

  /**
   * Applies the rebalance transform {@code (a + b) + c => a + (b + c)} given a {@link Concat} left
   * subtree.
   *
   * <p>Extracted so callers that already know their left side is a {@code Concat} — e.g. {@link
   * #append(Seq)}, where {@code this} is by definition a {@code Concat} — can skip the redundant
   * {@code instanceof} check that {@link #concat(Seq, Seq)} performs.
   *
   * @param left a {@code Concat} subtree; must not be {@code null}.
   * @param right the right sequence to append; must not be {@code null}.
   * @return a right-leaning {@code Concat} equivalent to {@code concat(left, right)}.
   */
  private static Concat growToTheRight(Concat left, Seq right) {
    // (a + b) + c => a + (b + c)
    return concat0(left.left, concat0(left.right, right));
  }

  private static Concat concat0(Seq left, Seq right) {
    return new Concat(
        left, right, left.length() + right.length(), left.elementCount() + right.elementCount());
  }

  /**
   * Recursively walks the rope tree rooted at {@code seq} and writes the {@link
   * AtomicSeq#upperCase()} of each leaf into {@code array}, starting at {@code index}.
   *
   * <p>This is the worker for {@link #upperCase()}. It avoids allocating an {@link Iterator} (and
   * the intermediate {@link java.util.ArrayList} used by {@link AtomicSeqIterable#upperCase()}) by
   * recursing into the tree directly and writing into a pre-sized array. Because {@link
   * #concat(Seq, Seq)} keeps the tree right-leaning, recursion depth is bounded by the depth of the
   * rope.
   *
   * <p>The dispatch is in priority order:
   *
   * <ol>
   *   <li>{@link AtomicSeq} — write a single uppercase leaf.
   *   <li>{@link Concat} — recurse into the {@code left} then {@code right} children directly,
   *       skipping the {@code Iterable} machinery.
   *   <li>Any other {@link AtomicSeqIterable} (e.g. {@link SeqArray}) — fall back to its iterator.
   * </ol>
   *
   * @param array the destination array; must have at least {@code seq.elementCount()} free slots
   *     from {@code index}.
   * @param index the next free slot in {@code array}.
   * @param seq the subtree to traverse.
   * @return the next free slot after writing all leaves of {@code seq}.
   * @throws IllegalStateException defensively, if {@code seq} is neither an {@link AtomicSeq} nor
   *     an {@link AtomicSeqIterable}.
   */
  private static int addUpperCase(AtomicSeq[] array, int index, Seq seq) {
    if (seq instanceof AtomicSeq) {
      array[index] = ((AtomicSeq) seq).upperCase();
      return index + 1;
    } else if (seq instanceof Concat) {
      Concat concat = (Concat) seq;
      index = addUpperCase(array, index, concat.left);
      return addUpperCase(array, index, concat.right);
    } else if (seq instanceof AtomicSeqIterable) {
      for (AtomicSeq atomicSeq : (AtomicSeqIterable) seq) {
        array[index++] = atomicSeq.upperCase();
      }
      return index;
    } else {
      throw new IllegalStateException("Unknown Seq type: " + seq.getClass());
    }
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(length);
    appendToInternal(sb);
    return sb.toString();
  }

  @Override
  public Seq prepend(Seq seq) {
    if (seq.isEmpty()) return this;
    return concat(seq, this);
  }

  @Override
  public Seq append(Seq seq) {
    if (seq.isEmpty()) return this;
    return growToTheRight(this, seq);
  }

  @Override
  public void appendTo(Appendable appendable) throws IOException {
    left.appendTo(appendable);
    right.appendTo(appendable);
  }

  @Override
  public void appendTo(StringBuilder sb) {
    sb.ensureCapacity(sb.length() + length);
    appendToInternal(sb);
  }

  /**
   * Recurses into the {@code left} and {@code right} children via their {@code appendToInternal},
   * skipping the per-level {@link StringBuilder#ensureCapacity(int)} call. {@inheritDoc}
   */
  @Override
  public void appendToInternal(StringBuilder sb) {
    left.appendToInternal(sb);
    right.appendToInternal(sb);
  }

  /**
   * Returns an iterator over the atomic (leaf) segments of this composite sequence. The iterator
   * performs a non-recursive, depth-first traversal of the sequence tree.
   *
   * @return an iterator over the {@link AtomicSeq} leaves.
   */
  @Override
  public Iterator<AtomicSeq> iterator() {
    // Initial deque size: at least 5 (the ArrayDeque default), at most elementCount —
    // clamping above elementCount avoids over-allocating for very short ropes.
    return new ConcatIterator(this, Math.min(Math.max(5, elementCount >> 2), elementCount));
  }

  /**
   * {@inheritDoc}
   *
   * <p>For {@code Concat}, this method pushes the {@code right} child onto the stack for later
   * traversal and returns the {@code left} child for immediate processing. This enables a
   * non-recursive, stack-based, depth-first traversal of the sequence tree.
   */
  @Override
  public Seq unfold(Deque<Seq> stack) {
    stack.push(right);
    return left;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Overrides the default {@link AtomicSeqIterable#upperCase()} with a direct, recursive variant
   * ({@link #addUpperCase}) that writes leaves into a pre-sized {@link AtomicSeq} array. Compared
   * to the default path this avoids allocating a {@link ConcatIterator} (and its backing {@link
   * java.util.ArrayDeque}) plus the intermediate {@link java.util.ArrayList}/{@code toArray()}
   * copy, which matters on this hot path for formats that uppercase composite ropes (e.g. {@code
   * %X}, {@code %E}).
   */
  @Override
  public Seq upperCase() {
    AtomicSeq[] buffer = new AtomicSeq[elementCount];
    addUpperCase(buffer, 0, this);
    return new SeqArray(buffer, length);
  }

  @Override
  public int elementCount() {
    return elementCount;
  }

  /**
   * An iterator that performs a non-recursive, depth-first traversal of the sequence tree.
   *
   * <p>It uses a {@link Deque} as a stack to manage the traversal, avoiding deep recursion and
   * potential {@link StackOverflowError}. The traversal logic is implemented as a trampoline, where
   * composite nodes are continuously "unfolded" via the {@link AtomicSeqIterable#unfold} method
   * until a leaf node (an {@link AtomicSeq}) is found.
   */
  private static final class ConcatIterator implements Iterator<AtomicSeq> {

    private final Deque<Seq> deque;

    ConcatIterator(Concat concat, int initialStackSize) {
      this.deque = new ArrayDeque<>(initialStackSize);
      // Start the traversal by pushing the root node onto the stack.
      // We don't need to special-case the constructor.
      deque.push(concat);
    }

    @Override
    public boolean hasNext() {
      return !deque.isEmpty();
    }

    @Override
    public AtomicSeq next() {
      // Start with the top of the stack. Throws NoSuchElementException if the deque is empty.
      Seq node = deque.pop();

      // This loop is a trampoline. It continuously unfolds composite nodes by calling
      // the 'unfold' method. The 'unfold' method returns the next node to process and
      // pushes any remaining children onto the stack. The loop terminates when a leaf
      // node (an AtomicSeq) is encountered.
      while (node instanceof AtomicSeqIterable) {
        node = ((AtomicSeqIterable) node).unfold(deque);
      }

      // We've found a leaf node.
      return (AtomicSeq) node;
    }
  }
}
