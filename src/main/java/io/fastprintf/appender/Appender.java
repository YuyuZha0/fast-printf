package io.fastprintf.appender;

import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;

import java.util.Iterator;
import java.util.List;

public interface Appender {

  void append(List<Seq> collect, Iterator<FormatTraits> traitsIterator);
}
