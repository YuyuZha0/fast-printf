package io.fastprintf.benchmark;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

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
Benchmark                                      Mode  Cnt     Score    Error  Units
FastPrintfBenchmark.fastPrintf                 avgt    6   662.068 ± 25.852  ns/op
FastPrintfBenchmark.fastPrintfPrimitive        avgt    6   644.819 ± 31.485  ns/op
FastPrintfBenchmark.fastPrintfWithThreadLocal  avgt    6   650.900 ± 21.227  ns/op
FastPrintfBenchmark.jdkPrintf                  avgt    6  1544.709 ± 18.232  ns/op
 *     </pre>
 */
public class FastPrintfBenchmark {

  private static final int ARRAY_SIZE = 24;
  private static final String FORMAT =
      "This is plain text: % d %% %-8X %% %#08o %% %.3f %% %-7.5g %% %+10.5e";
  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(FORMAT);
  private static final FastPrintf FAST_PRINTF2 = FAST_PRINTF.enableThreadLocalCache();

  private long[] longValues;
  private double[] doubleValues;
  private int index;
  private long v1;
  private long v2;
  private long v3;
  private double d1;
  private double d2;
  private double d3;

  @Setup(Level.Trial)
  public void setupTrial() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    longValues = random.longs(ARRAY_SIZE).toArray();
    doubleValues = random.doubles(ARRAY_SIZE).map(d -> d * 1e6).toArray();
    index = 0;
  }

  @Setup(Level.Invocation)
  public void setupInvocation() {
    index = (index + 1) % (ARRAY_SIZE - 3);
    v1 = longValues[index];
    v2 = longValues[index + 1];
    v3 = longValues[index + 2];
    d1 = doubleValues[index];
    d2 = doubleValues[index + 2];
    d3 = doubleValues[index + 3];
  }

  @Benchmark
  public String fastPrintf() {
    return FAST_PRINTF.format(v1, v2, v3, d1, d2, d3);
  }

  @Benchmark
  public String fastPrintfPrimitive() {
    Args args =
        Args.createWithExpectedSize(6)
            .putLong(v1)
            .putLong(v2)
            .putLong(v3)
            .putDouble(d1)
            .putDouble(d2)
            .putDouble(d3);
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    return FAST_PRINTF2.format(v1, v2, v3, d1, d2, d3);
  }

  @Benchmark
  public String jdkPrintf() {
    return String.format(FORMAT, v1, v2, v3, d1, d2, d3);
  }
}
