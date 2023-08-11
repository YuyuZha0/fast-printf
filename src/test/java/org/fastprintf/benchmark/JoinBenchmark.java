package org.fastprintf.benchmark;

import org.fastprintf.FastPrintf;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 5)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
/**
 *
 *
 * <pre>
 *     Benchmark                                Mode  Cnt    Score   Error  Units
 * JoinBenchmark.fastPrintf                 avgt    6  280.292 ± 1.286  ns/op
 * JoinBenchmark.fastPrintfWithThreadLocal  avgt    6  265.075 ± 1.837  ns/op
 * JoinBenchmark.stringJoin                 avgt    6   93.775 ± 5.566  ns/op
 * </pre>
 */
public class JoinBenchmark {

  private static final FastPrintf FAST_PRINTF = FastPrintf.compile("%s, %s, %s, %s, %s, %s");
  private static final FastPrintf FAST_PRINTF2 = FAST_PRINTF.enableThreadLocalCache();
  private List<String> strings;

  private Iterator<String> iterator;

  @Setup
  public void setup() {
    strings =
        ThreadLocalRandom.current()
            .longs(6 * 300)
            .mapToObj(Long::toString)
            .collect(Collectors.toList());
    iterator = strings.iterator();
  }

  public String next() {
    if (iterator.hasNext()) {
      return iterator.next();
    } else {
      iterator = strings.iterator();
      return next();
    }
  }

  @Benchmark
  public String stringJoin() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 6; ++i) {
      sb.append(next());
      if (i != 5) sb.append(", ");
    }
    return sb.toString();
  }

  @Benchmark
  public String fastPrintf() {
    return FAST_PRINTF.format(next(), next(), next(), next(), next(), next());
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    return FAST_PRINTF2.format(next(), next(), next(), next(), next(), next());
  }
}
