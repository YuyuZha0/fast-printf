package io.fastprintf.traits;

import static org.junit.Assert.*;

import io.fastprintf.PrintfException;
import io.fastprintf.seq.Seq;
import java.lang.reflect.Field;
import org.junit.Test;

public class CharSequenceTraitsTest {

  @Test
  public void testAsSeq_Optimization() throws Exception {
    // To verify the optimization, we need a string with length > 1.
    // If length is 0 or 1, Seq.lazy() optimizes to EmptySeq or Repeated,
    // skipping the LazySeq wrapper.
    CharSequenceTraits traits = new CharSequenceTraits("OptimizeMe");
    Seq seq = traits.asSeq(); // Returns a LazySeq

    // Verify via reflection that LazySeq is holding 'traits' as the action
    // rather than a new lambda object.
    Field actionField = seq.getClass().getDeclaredField("action");
    actionField.setAccessible(true);
    Object action = actionField.get(seq);

    assertSame("Should pass itself as the Consumer to avoid allocation", traits, action);
  }

  @Test
  public void testAsSeq_StringInput() {
    // Tests the 'if (value instanceof String)' branch in accept()
    verifyAsSeq("Hello World");
  }

  @Test
  public void testAsSeq_StringBuilderInput() {
    // Tests the 'else' branch in accept()
    verifyAsSeq(new StringBuilder("Mutable Sequence"));
  }

  private void verifyAsSeq(CharSequence input) {
    CharSequenceTraits traits = new CharSequenceTraits(input);
    Seq seq = traits.asSeq();

    assertEquals("Length mismatch", input.length(), seq.length());
    assertEquals("Content mismatch", input.toString(), seq.toString());

    StringBuilder sb = new StringBuilder();
    seq.appendTo(sb);
    assertEquals(input.toString(), sb.toString());
  }

  @Test
  public void testConstructorAndRef_withString() {
    String str = "test";
    CharSequenceTraits traits = new CharSequenceTraits(str);

    assertNotNull(traits);
    RefSlot ref = traits.ref();
    assertFalse("RefSlot should not be primitive", ref.isPrimitive());
    assertSame("RefSlot should hold the original String object", str, ref.get());
  }

  @Test
  public void testConstructorAndRef_withStringBuilder() {
    StringBuilder sb = new StringBuilder("builder test");
    CharSequenceTraits traits = new CharSequenceTraits(sb);

    assertNotNull(traits);
    RefSlot ref = traits.ref();
    assertFalse("RefSlot should not be primitive", ref.isPrimitive());
    assertSame("RefSlot should hold the original StringBuilder object", sb, ref.get());
  }

  @Test
  public void testAsString() {
    CharSequenceTraits traits = new CharSequenceTraits("hello world");
    assertEquals("hello world", traits.asString());

    CharSequenceTraits traitsBuilder = new CharSequenceTraits(new StringBuilder("from builder"));
    assertEquals("from builder", traitsBuilder.asString());
  }

  @Test
  public void testAsInt_Success() {
    CharSequenceTraits traits = new CharSequenceTraits("12345");
    assertEquals(12345, traits.asInt());

    CharSequenceTraits traitsNegative = new CharSequenceTraits("-54321");
    assertEquals(-54321, traitsNegative.asInt());
  }

  @Test(expected = PrintfException.class)
  public void testAsInt_Failure_NotANumber() {
    new CharSequenceTraits("abc").asInt();
  }

  @Test(expected = PrintfException.class)
  public void testAsInt_Failure_TooLarge() {
    new CharSequenceTraits("99999999999999").asInt();
  }

  @Test
  public void testAsChar_Success() {
    CharSequenceTraits traits = new CharSequenceTraits("Hello");
    assertEquals('H', traits.asChar());

    CharSequenceTraits singleCharTraits = new CharSequenceTraits("Z");
    assertEquals('Z', singleCharTraits.asChar());
  }

  @Test(expected = PrintfException.class)
  public void testAsChar_Failure_EmptyString() {
    new CharSequenceTraits("").asChar();
  }

  @Test
  public void testAsIntForm_Success() {
    CharSequenceTraits traits = new CharSequenceTraits("9876543210"); // Fits in long
    assertEquals("9876543210", traits.asIntForm().toDecimalString());
  }

  @Test(expected = PrintfException.class)
  public void testAsIntForm_Failure() {
    new CharSequenceTraits("not a long").asIntForm();
  }

  @Test
  public void testAsFloatForm_Success() {
    CharSequenceTraits traits = new CharSequenceTraits("123.456");
    assertEquals("123.456", traits.asFloatForm().toString());

    CharSequenceTraits traitsScientific = new CharSequenceTraits("1.23e-4");
    assertEquals("1.23E-4", traitsScientific.asFloatForm().toString());
  }

  @Test(expected = PrintfException.class)
  public void testAsFloatForm_Failure() {
    new CharSequenceTraits("not a double").asFloatForm();
  }

  @Test
  public void testAsObject() {
    String originalValue = "original";
    CharSequenceTraits traits = new CharSequenceTraits(originalValue);
    Object obj = traits.asObject();

    assertSame("asObject() must return the identical CharSequence instance", originalValue, obj);
  }

  // --- Test Unsupported Operations ---

  @Test(expected = PrintfException.class)
  public void testAsTemporalAccessor_throwsException() {
    new CharSequenceTraits("2022-01-01").asTemporalAccessor();
  }
}
