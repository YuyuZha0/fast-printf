package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.number.FloatLayout;
import io.fastprintf.number.IntForm;
import io.fastprintf.seq.Seq;
import org.junit.Test;

public class CharacterTraitsTest {

  @Test
  public void testAsSeq() {
    char c = 'Z';
    CharacterTraits traits = CharacterTraits.ofPrimitive(c);

    Seq seq = traits.asSeq();

    // 1. Verify Length
    assertEquals(1, seq.length());

    // 2. Verify Content
    assertEquals("Z", seq.toString());

    // 3. Verify Fast Path Append
    StringBuilder sb = new StringBuilder();
    seq.appendTo(sb);
    assertEquals("Z", sb.toString());
  }

  @Test
  public void testOfPrimitive_createsPrimitiveTraits() {
    char primitiveValue = 'X';
    CharacterTraits traits = CharacterTraits.ofPrimitive(primitiveValue);

    assertEquals(primitiveValue, traits.asChar());
    assertTrue("ref() should indicate a primitive source", traits.ref().isPrimitive());

    Object obj = traits.asObject();
    assertTrue("asObject() should return a boxed Character", obj instanceof Character);
    assertEquals(primitiveValue, ((Character) obj).charValue());
  }

  @Test
  public void testConstructor_createsBoxedTraits() {
    Character boxedValue = new Character('Y');
    CharacterTraits traits = new CharacterTraits(boxedValue, RefSlot.of(boxedValue));

    assertEquals(boxedValue.charValue(), traits.asChar());
    assertFalse("ref() should indicate a non-primitive source", traits.ref().isPrimitive());
    assertSame("ref().get() should return the original object", boxedValue, traits.ref().get());
    assertSame("asObject() should return the original object", boxedValue, traits.asObject());
  }

  @Test
  public void testConversions_StandardChar() {
    char value = 'A'; // ASCII 65
    CharacterTraits traits = CharacterTraits.ofPrimitive(value);

    assertEquals("A", traits.asString());
    assertEquals(65, traits.asInt());
    assertEquals('A', traits.asChar());

    IntForm intForm = traits.asIntForm();
    assertEquals("65", intForm.toDecimalString());
    assertEquals(1, intForm.signum());

    // Correct way to test FloatForm
    FloatLayout layout = traits.asFloatForm().decimalLayout(1);
    assertEquals("65", layout.getMantissa().toString());
  }

  @Test
  public void testConversions_NullChar() {
    char value = '\0'; // ASCII 0
    CharacterTraits traits = CharacterTraits.ofPrimitive(value);

    assertEquals("\0", traits.asString());
    assertEquals(0, traits.asInt());
    assertEquals('\0', traits.asChar());

    IntForm intForm = traits.asIntForm();
    assertEquals("0", intForm.toDecimalString());
    assertEquals(0, intForm.signum());
  }

  @Test
  public void testConversions_NonAsciiChar() {
    char value = '€'; // Euro sign, Unicode U+20AC, decimal 8364
    CharacterTraits traits = CharacterTraits.ofPrimitive(value);

    assertEquals("€", traits.asString());
    assertEquals(8364, traits.asInt());
    assertEquals('€', traits.asChar());

    IntForm intForm = traits.asIntForm();
    assertEquals("8364", intForm.toDecimalString());
  }

  @Test
  public void testRefAndAsObjectBehavior() {
    // --- Primitive Case ---
    CharacterTraits primitiveTraits = CharacterTraits.ofPrimitive('p');
    assertTrue("ref() should be primitive for ofPrimitive()", primitiveTraits.ref().isPrimitive());

    Object primitiveAsObject = primitiveTraits.asObject();
    assertTrue(
        "asObject() from primitive should return a Character",
        primitiveAsObject instanceof Character);
    assertEquals('p', primitiveAsObject);

    // --- Boxed Case ---
    Character originalBoxedChar = new Character('q');
    CharacterTraits boxedTraits =
        new CharacterTraits(originalBoxedChar, RefSlot.of(originalBoxedChar));
    assertFalse(
        "ref() should not be primitive for a boxed Character", boxedTraits.ref().isPrimitive());

    Object boxedAsObject = boxedTraits.asObject();
    assertSame(
        "asObject() from boxed should return the identical instance",
        originalBoxedChar,
        boxedAsObject);
  }

  // --- Test Unsupported Operations ---

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    CharacterTraits.ofPrimitive('T').asTemporalAccessor();
  }
}
