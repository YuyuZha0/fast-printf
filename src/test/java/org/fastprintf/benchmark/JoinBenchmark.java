package org.fastprintf.benchmark;

import org.fastprintf.Args;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
 * Benchmark                                Mode  Cnt    Score    Error  Units
 * JoinBenchmark.fastPrintf                 avgt    6  445.875 ±  6.025  ns/op
 * JoinBenchmark.fastPrintfWithThreadLocal  avgt    6  449.232 ± 10.131  ns/op
 * JoinBenchmark.stringJoin                 avgt    6  293.116 ± 17.708  ns/op
 * </pre>
 */
public class JoinBenchmark {

  private static final FastPrintf FAST_PRINTF = FastPrintf.compile("%s, %s, %s, %s, %s, %s");
  private static final FastPrintf FAST_PRINTF2 = FAST_PRINTF.enableThreadLocalCache();
  private List<Object> objects;
  private Iterator<Object> iterator;

  @Setup
  public void setup() {
    List<Object> objects = new ArrayList<>(300 * 6);
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < 300; ++i) {
      objects.add(random.nextInt());
      objects.add(random.nextLong());
      objects.add(random.nextFloat());
      objects.add(random.nextDouble());
      objects.add(random.nextBoolean());
      byte[] bytes = new byte[16];
      random.nextBytes(bytes);
      objects.add(new String(bytes, StandardCharsets.UTF_8));
    }
    this.objects = objects;
    iterator = objects.iterator();
  }

  public Object next() {
    if (iterator.hasNext()) {
      return iterator.next();
    } else {
      iterator = objects.iterator();
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
    Args args = Args.of(next(), next(), next(), next(), next(), next());
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    Args args = Args.of(next(), next(), next(), next(), next(), next());
    return FAST_PRINTF2.format(args);
  }
}
