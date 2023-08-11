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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 5)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
/*
 *
 *
 * <pre>
Benchmark                                      Mode  Cnt     Score     Error  Units
FastPrintfBenchmark.fastPrintf                 avgt    6   907.471 ±  23.232  ns/op
FastPrintfBenchmark.fastPrintfWithThreadLocal  avgt    6   922.191 ±  29.155  ns/op
FastPrintfBenchmark.jdkPrintf                  avgt    6  3786.070 ± 132.175  ns/op
FastPrintfBenchmark.stringJoin                 avgt    6   233.571 ±   8.562  ns/op
 *     </pre>
 */
public class FastPrintfBenchmark {

  private static final String FORMAT =
      "This is plain text: % d %% %-8X %% %#08o %% %.3f %% %-7.5g %% %+10.5e";
  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(FORMAT);
  private static final FastPrintf FAST_PRINTF2 = FAST_PRINTF.enableThreadLocalCache();

  private long[] numbers;
  private int index;

  @Setup
  public void setup() {
    numbers = ThreadLocalRandom.current().longs(1024).toArray();
    index = 0;
  }

  private long nextLong() {
    return numbers[(index++) & 1023];
  }

  @Benchmark
  public String fastPrintf() {
    Long v = nextLong();
    return FAST_PRINTF.format(v, v, v, v, v, v);
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    Long v = nextLong();
    return FAST_PRINTF2.format(v, v, v, v, v, v);
  }

  @Benchmark
  public String jdkPrintf() {
    Long l = nextLong();
    Double d = l.doubleValue();
    return String.format(FORMAT, l, l, l, d, d, d);
  }

  @Benchmark
  public String stringJoin() {
    long l = nextLong();
    double d = l;
    StringBuilder builder = new StringBuilder(255);
    builder.append("This is plain text: ");
    builder.append(l);
    builder.append(" % ");
    builder.append(Long.toHexString(l));
    builder.append(" % ");
    builder.append(Long.toOctalString(l));
    builder.append(" % ");
    builder.append(d);
    builder.append(" % ");
    builder.append(d);
    builder.append(" % ");
    builder.append(d);
    return builder.toString();
  }
}
