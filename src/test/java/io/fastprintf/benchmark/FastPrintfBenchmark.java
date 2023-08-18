package io.fastprintf.benchmark;

import io.fastprintf.FastPrintf;
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
FastPrintfBenchmark.fastPrintf                 avgt    6   906.942 ±  14.372  ns/op
FastPrintfBenchmark.fastPrintfWithThreadLocal  avgt    6  1009.801 ± 127.089  ns/op
FastPrintfBenchmark.jdkPrintf                  avgt    6  3766.802 ± 129.694  ns/op
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
}
