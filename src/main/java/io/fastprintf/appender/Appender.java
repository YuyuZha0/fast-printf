package io.fastprintf.appender;

import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;
import java.util.Iterator;
import java.util.function.Consumer;

public interface Appender {

  void append(Consumer<? super Seq> collect, Iterator<FormatTraits> traitsIterator);
}
