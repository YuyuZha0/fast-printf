package io.fastprintf.seq;

import io.fastprintf.util.Preconditions;
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
 * <h3>Architectural Benefits:</h3>
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
public final class Concat implements Seq, Iterable<AtomicSeq> {

  private final Seq left;
  private final Seq right;
  private final int length;

  private Concat(Seq left, Seq right, int length) {
    this.left = left;
    this.right = right;
    this.length = length;
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
    int length = left.length() + right.length();
    return new Concat(left, right, length);
  }

  /**
   * Helper method for building a new {@code Seq} tree during operations like {@code subSequence}
   * and {@code upperCase}.
   */
  private static Seq prependHead(Seq currentHead, AtomicSeq seq) {
    if (currentHead != null) {
      return concat(currentHead, seq);
    }
    return seq;
  }

  @Override
  public int length() {
    return length;
  }

  /**
   * Returns the character at the specified index.
   *
   * <p><b>Performance Note:</b> This is an O(S) operation, where S is the number of atomic segments
   * in the sequence. It requires traversing the tree from the beginning to find the correct
   * segment. It is therefore much slower than {@code charAt} on a standard {@link String} or {@link
   * AtomicSeq}.
   *
   * @param index the index of the character to return.
   * @return the character at the specified index.
   * @throws IndexOutOfBoundsException if the index is out of range.
   */
  @Override
  public char charAt(int index) {
    Preconditions.checkPositionIndex(index, length);
    for (AtomicSeq seq : this) {
      if (index < seq.length()) {
        return seq.charAt(index);
      }
      index -= seq.length();
    }
    throw new AssertionError(); // Should be unreachable
  }

  /**
   * Returns a new {@code Seq} that is a subsequence of this sequence.
   *
   * <p><b>Performance Note:</b> This is an O(S) operation, where S is the number of atomic
   * segments. It reconstructs a new sequence tree composed of views of the underlying atomic
   * segments. No character data is copied.
   *
   * @param start the start index, inclusive.
   * @param end the end index, exclusive.
   * @return the specified subsequence.
   */
  @Override
  public Seq subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, length);
    if (start == end) return Seq.empty();
    if (start == 0 && end == length) return this;
    Seq head = null;
    for (AtomicSeq seq : this) {
      int seqLength = seq.length();
      if (start < seqLength) {
        if (end <= seqLength) {
          head = prependHead(head, seq.subSequence(start, end));
          break;
        }
        head = prependHead(head, seq.subSequence(start, seqLength));
        start = 0;
        end -= seqLength;
      } else {
        start -= seqLength;
        end -= seqLength;
      }
    }
    assert head != null;
    return head;
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
   * Creates a new, uppercased {@code Seq} by eagerly transforming each atomic segment.
   *
   * @return a new sequence containing the uppercased content.
   */
  @Override
  public Seq upperCase() {
    Seq head = null;
    for (AtomicSeq seq : this) {
      head = prependHead(head, seq.upperCase());
    }
    return head;
  }

  /**
   * Appends the contents of this sequence to an {@link Appendable}. This is the primary rendering
   * method and is highly efficient.
   *
   * @param appendable the destination {@code Appendable}.
   * @throws IOException if an I/O error occurs.
   */
  @Override
  public void appendTo(Appendable appendable) throws IOException {
    for (AtomicSeq seq : this) {
      seq.appendTo(appendable);
    }
  }

  /**
   * Appends the contents of this sequence to a {@link StringBuilder}. This is a specialized, highly
   * efficient rendering method that leverages optimized appends.
   *
   * @param sb the destination {@code StringBuilder}.
   */
  @Override
  public void appendTo(StringBuilder sb) {
    for (AtomicSeq seq : this) {
      seq.appendTo(sb);
    }
  }

  /**
   * Returns the index within this sequence of the first occurrence of the specified character.
   *
   * <p><b>Performance Note:</b> This is an O(L) operation, where L is the total length.
   *
   * @param c the character to search for.
   * @return the index of the first occurrence, or -1 if not found.
   */
  @Override
  public int indexOf(char c) {
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
   * Returns an iterator over the atomic (leaf) segments of this composite sequence. The iterator
   * performs a non-recursive, depth-first traversal of the sequence tree.
   *
   * @return an iterator over the {@link AtomicSeq} leaves.
   */
  @Override
  public Iterator<AtomicSeq> iterator() {
    return new ConcatIterator(this);
  }

  /**
   * An iterator that performs a non-recursive, depth-first traversal of the {@code Concat} tree. It
   * uses a {@link Deque} as a stack to manage the traversal, avoiding deep recursion and potential
   * {@link StackOverflowError}.
   */
  private static final class ConcatIterator implements Iterator<AtomicSeq> {

    private final Deque<Seq> deque = new ArrayDeque<>(8);

    ConcatIterator(Concat concat) {
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
      // Start with the top of the stack.
      Seq node = deque.pop();

      // While the current node is an internal node (Concat), keep digging.
      // This loop will find the next left-most leaf.
      while (node instanceof Concat) {
        Concat concat = (Concat) node;
        // Push the right child to be visited later.
        deque.push(concat.right);
        // Descend to the left child for the next iteration.
        node = concat.left;
      }

      // We've found a leaf node.
      return (AtomicSeq) node;
    }
  }
}
