package org.fastprintf.appender;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

public interface Appender {

  void append(List<Seq> collect, Iterator<FormatTraits> traitsIterator);

}
