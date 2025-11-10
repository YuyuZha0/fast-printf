package io.fastprintf.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PreconditionsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  // --- Tests for checkNotNull() ---

  @Test
  public void checkNotNull_withNonNull_shouldReturnObject() {
    String testObject = "I am not null";
    String result = Preconditions.checkNotNull(testObject, "testObject");
    assertEquals("The returned object should be the same as the input", testObject, result);
  }

  @Test
  public void checkNotNull_withNull_shouldThrowNPE() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(containsString("Argument 'myArg' cannot be null"));

    Preconditions.checkNotNull(null, "myArg");
  }

  // --- Tests for checkArgument() ---

  @Test
  public void checkArgument_withTrueCondition_shouldNotThrow() {
    Preconditions.checkArgument(true, "This should not be thrown");
    Preconditions.checkArgument(true, "Error with %s", "args");
  }

  @Test
  public void checkArgument_withFalseCondition_shouldThrowIAE() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Simple error message");

    Preconditions.checkArgument(false, "Simple error message");
  }

  @Test
  public void checkArgument_withFalseConditionAndArgs_shouldThrowIAE() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid value: 42 for user test");

    Preconditions.checkArgument(false, "Invalid value: %s for user %s", 42, "test");
  }

  // --- Tests for checkPositionIndex() ---

  @Test
  public void checkPositionIndex_withValidIndices_shouldNotThrow() {
    Preconditions.checkPositionIndex(0, 1); // Index 0 should be valid for size 1
    Preconditions.checkPositionIndex(0, 5); // Index 0 should be valid for size 5
    Preconditions.checkPositionIndex(4, 5); // Index 4 should be valid for size 5
    Preconditions.checkPositionIndex(2, 5); // Index 2 should be valid for size 5
  }

  @Test
  public void checkPositionIndex_withNegativeIndex_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("index: -1, size: 5"));

    Preconditions.checkPositionIndex(-1, 5);
  }

  @Test
  public void checkPositionIndex_withIndexEqualToSize_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("index: 5, size: 5"));

    Preconditions.checkPositionIndex(5, 5);
  }

  @Test
  public void checkPositionIndex_withIndexGreaterThanSize_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("index: 6, size: 5"));

    Preconditions.checkPositionIndex(6, 5);
  }

  @Test
  public void checkPositionIndex_withSizeZero_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("index: 0, size: 0"));

    Preconditions.checkPositionIndex(0, 0);
  }

  // --- Tests for checkPositionIndexes() ---

  @Test
  public void checkPositionIndexes_withValidRanges_shouldNotThrow() {
    Preconditions.checkPositionIndexes(0, 5, 5); // Full range [0, 5) in size 5
    Preconditions.checkPositionIndexes(1, 4, 5); // Sub-range [1, 4) in size 5
    Preconditions.checkPositionIndexes(0, 0, 5); // Empty range at start [0, 0) in size 5
    Preconditions.checkPositionIndexes(5, 5, 5); // Empty range at end [5, 5) in size 5
    Preconditions.checkPositionIndexes(2, 2, 5); // Empty range in middle [2, 2) in size 5
    Preconditions.checkPositionIndexes(0, 0, 0); // Empty range [0, 0) in size 0
  }

  @Test
  public void checkPositionIndexes_withNegativeStart_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("start: -1, end: 2, size: 5"));

    Preconditions.checkPositionIndexes(-1, 2, 5);
  }

  @Test
  public void checkPositionIndexes_withEndLessThanStart_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("start: 3, end: 2, size: 5"));

    Preconditions.checkPositionIndexes(3, 2, 5);
  }

  @Test
  public void checkPositionIndexes_withEndGreaterThanSize_shouldThrowIOOBE() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage(containsString("start: 1, end: 6, size: 5"));

    Preconditions.checkPositionIndexes(1, 6, 5);
  }
}
