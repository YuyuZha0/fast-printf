package io.fastprintf.seq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.Test;

/**
 * This test class is specifically designed to achieve 100% code coverage for the default methods on
 * the {@link Seq} interface.
 *
 * <p>It uses a minimal, test-only implementation of {@code Seq} that intentionally does NOT
 * override the default methods, forcing them to be executed.
 */
public class SeqDefaultMethodsTest {

  @Test
  public void testDefaultPrepend() {
    Seq seq = new DefaultSeqImpl("world");
    Seq result = seq.prepend(Seq.wrap("hello "));
    assertTrue("Default prepend should return a Concat node", result instanceof Concat);
    assertEquals("hello world", result.toString());
  }

  @Test
  public void testDefaultAppend() {
    Seq seq = new DefaultSeqImpl("hello");
    Seq result = seq.append(Seq.wrap(" world"));
    assertTrue("Default append should return a Concat node", result instanceof Concat);
    assertEquals("hello world", result.toString());
  }

  @Test
  public void testDefaultAppendToAppendable() throws IOException {
    final String content = "test-content";
    Seq seq = new DefaultSeqImpl(content);

    final AtomicInteger appendCharCount = new AtomicInteger(0);
    StringWriter writer = new StringWriter();
    Appendable countingAppendable =
        new Appendable() {
          @Override
          public Appendable append(CharSequence csq) throws IOException {
            return writer.append(csq);
          }

          @Override
          public Appendable append(CharSequence csq, int start, int end) throws IOException {
            return writer.append(csq, start, end);
          }

          @Override
          public Appendable append(char c) throws IOException {
            appendCharCount.incrementAndGet();
            return writer.append(c);
          }
        };

    seq.appendTo(countingAppendable);

    assertEquals("Output should match", content, writer.toString());
    assertEquals(
        "Should have called append(char) for each character",
        content.length(),
        appendCharCount.get());
  }

  @Test
  public void testDefaultAppendToStringBuilder() {
    Seq seq = new DefaultSeqImpl("test-sb");
    StringBuilder sb = new StringBuilder("prefix-");

    seq.appendTo(sb);

    assertEquals("prefix-test-sb", sb.toString());
  }

  @Test
  public void testDefaultDup() {
    Seq seq = new DefaultSeqImpl("data");
    Seq duped = seq.dup();
    assertSame("dup() should return the same instance", seq, duped);
  }

  @Test
  public void testDefaultMap() {
    Seq seq = new DefaultSeqImpl("body");
    Function<Seq, Seq> addHeaderFooter = s -> Seq.concat(Seq.wrap("H:"), s).append(Seq.wrap(":F"));

    Seq result = seq.map(addHeaderFooter);

    assertEquals("H:body:F", result.toString());
  }

  @Test
  public void testDefaultIsEmpty() {
    assertTrue(new DefaultSeqImpl("").isEmpty());
    assertFalse(new DefaultSeqImpl("not-empty").isEmpty());
  }

  @Test
  public void testDefaultIndexOf() {
    Seq seq = new DefaultSeqImpl("ab-c-de");
    assertEquals(2, seq.indexOf('-'));
    assertEquals(0, seq.indexOf('a'));
    assertEquals(6, seq.indexOf('e'));
    assertEquals(Seq.INDEX_NOT_FOUND, seq.indexOf('z'));
  }

  @Test
  public void testDefaultStartsWith() {
    Seq seq = new DefaultSeqImpl("start");
    assertTrue(seq.startsWith('s'));
    assertFalse(seq.startsWith('t'));
    assertFalse(
        "startsWith should be false for an empty sequence", new DefaultSeqImpl("").startsWith('a'));
  }

  // A minimal, test-only implementation of Seq that relies on the default methods.
  private static class DefaultSeqImpl implements AtomicSeq {
    private final String value;

    DefaultSeqImpl(String value) {
      this.value = value;
    }

    @Override
    public int length() {
      return value.length();
    }

    @Override
    public char charAt(int index) {
      return value.charAt(index);
    }

    @Override
    public AtomicSeq subSequence(int start, int end) {
      return new DefaultSeqImpl(value.substring(start, end));
    }

    @Override
    public AtomicSeq upperCase() {
      return new DefaultSeqImpl(value.toUpperCase());
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
