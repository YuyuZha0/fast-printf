package io.fastprintf.appender;

import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;
import java.util.Iterator;
import java.util.function.Consumer;

public final class FixedStringAppender implements Appender {

  private final String value;
  private final Seq seq;

  public FixedStringAppender(String value) {
    this.value = Preconditions.checkNotNull(value, "value");
    this.seq = Seq.wrap(value);
  }

  @Override
  public void append(Consumer<? super Seq> collect, Iterator<FormatTraits> traitsIterator) {
    collect.accept(seq);
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    int length = value.length();
    StringBuilder sb = new StringBuilder(length + 5);
    sb.append('"');
    for (int i = 0; i < length; ++i) {
      char c = value.charAt(i);
      switch (c) {
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '"':
          sb.append("\\\"");
          break;
        default:
          sb.append(c);
      }
    }
    sb.append('"');
    return sb.toString();
  }
}
