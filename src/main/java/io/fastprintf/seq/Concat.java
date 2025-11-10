package io.fastprintf.seq;

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
    // let the tree grow to the right, so the deque stack max size could be smaller
    if (left instanceof Concat) {
      Concat leftConcat = (Concat) left;
      return concat0(leftConcat.left, concat0(leftConcat.right, right));
    } else {
      return concat0(left, right);
    }
  }

  private static Concat concat0(Seq left, Seq right) {
    return new Concat(
        left, right, left.length() + right.length(), left.elementCount() + right.elementCount());
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(length);
    appendTo(sb);
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
    return concat(this, seq);
  }

  /**
   * Returns an iterator over the atomic (leaf) segments of this composite sequence. The iterator
   * performs a non-recursive, depth-first traversal of the sequence tree.
   *
   * @return an iterator over the {@link AtomicSeq} leaves.
   */
  @Override
  public Iterator<AtomicSeq> iterator() {
    return new ConcatIterator(this, Math.max(5, elementCount >> 2));
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
