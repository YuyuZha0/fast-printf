package org.fastprintf.appender;

import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class FixedStringAppender implements Appender {

  private final String value;

  public FixedStringAppender(String value) {
    this.value = Objects.requireNonNull(value);
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
    return '`' + value + '`';
  }
}
