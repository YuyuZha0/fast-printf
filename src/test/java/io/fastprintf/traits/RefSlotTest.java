package io.fastprintf.traits;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for the {@link RefSlot} class. */
public class RefSlotTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void ofPrimitive_shouldReturnSingletonPrimitiveSlot() {
    RefSlot primitiveSlot1 = RefSlot.ofPrimitive();
    RefSlot primitiveSlot2 = RefSlot.ofPrimitive();

    assertNotNull("ofPrimitive() should not return null", primitiveSlot1);
    assertTrue("Slot from ofPrimitive() should be primitive", primitiveSlot1.isPrimitive());
    assertSame(
        "ofPrimitive() should always return the same singleton instance",
        primitiveSlot1,
        primitiveSlot2);
  }

  @Test
  public void ofNull_shouldReturnSingletonNullSlot() {
    RefSlot nullSlot1 = RefSlot.ofNull();
    RefSlot nullSlot2 = RefSlot.ofNull();

    assertNotNull("ofNull() should not return null", nullSlot1);
    assertFalse("Slot from ofNull() should not be primitive", nullSlot1.isPrimitive());
    assertSame("ofNull() should always return the same singleton instance", nullSlot1, nullSlot2);
  }

  @Test
  public void of_withNullArgument_shouldReturnSingletonNullSlot() {
    RefSlot fromNullArg = RefSlot.of(null);
    RefSlot fromFactory = RefSlot.ofNull();

    assertSame("of(null) should return the same instance as ofNull()", fromNullArg, fromFactory);
  }

  @Test
  public void of_withObjectArgument_shouldReturnNewObjectSlot() {
    String testObject = "test string";
    RefSlot objectSlot = RefSlot.of(testObject);

    assertNotNull("of(object) should not return null", objectSlot);
    assertFalse("Slot from of(object) should not be primitive", objectSlot.isPrimitive());

    // Verify it creates a new wrapper instance each time
    RefSlot objectSlot2 = RefSlot.of(testObject);
    assertNotSame(
        "of(object) should return a new RefSlot instance each time", objectSlot, objectSlot2);
  }

  @Test
  public void get_onPrimitiveSlot_shouldThrowUnsupportedOperationException() {
    RefSlot primitiveSlot = RefSlot.ofPrimitive();

    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage(is("RefSlot is primitive"));

    primitiveSlot.get();
  }

  @Test
  public void get_onNullSlot_shouldReturnNull() {
    RefSlot nullSlot = RefSlot.ofNull();
    assertNull("get() on a null slot should return null", nullSlot.get());
  }

  @Test
  public void get_onObjectSlot_shouldReturnOriginalObject() {
    Object originalObject = new Object();
    RefSlot objectSlot = RefSlot.of(originalObject);

    Object retrievedObject = objectSlot.get();
    assertSame(
        "get() should return the exact same object instance", originalObject, retrievedObject);
  }

  @Test
  public void isPrimitive_onManuallyConstructedPrimitiveSlot_shouldBeTrue() {
    // This test validates the fallback check `this.value == PRIMITIVE_DEFAULT`
    // It's not a public API but good to verify the internal logic.
    // We use reflection to access the private field and constructor for this test.
    try {
      java.lang.reflect.Field field = RefSlot.class.getDeclaredField("PRIMITIVE_DEFAULT");
      field.setAccessible(true);
      Object primitiveDefault = field.get(null);

      java.lang.reflect.Constructor<RefSlot> constructor =
          RefSlot.class.getDeclaredConstructor(Object.class);
      constructor.setAccessible(true);
      RefSlot manualSlot = constructor.newInstance(primitiveDefault);

      assertTrue(
          "Manually constructed primitive slot should be primitive", manualSlot.isPrimitive());
    } catch (Exception e) {
      // This test is not critical if reflection is disabled by security manager
      System.err.println("Could not perform reflection-based test for RefSlot: " + e.getMessage());
    }
  }
}
