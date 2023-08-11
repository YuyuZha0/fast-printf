package org.fastprintf.appender;

import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;
import org.fastprintf.util.Preconditions;

import java.util.Iterator;
import java.util.List;

public final class FixedStringAppender implements Appender {

  private final String value;

  public FixedStringAppender(String value) {
    this.value = Preconditions.checkNotNull(value, "value");
  }

  @Override
  public void append(List<Seq> collect, Iterator<FormatTraits> traitsIterator) {
    collect.add(Seq.wrap(value));
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
