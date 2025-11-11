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
Benchmark                                      Mode  Cnt     Score     Error  Units
FastPrintfBenchmark.fastPrintf                 avgt    6  1014.888 ±  65.189  ns/op
FastPrintfBenchmark.fastPrintfPrimitive        avgt    6  1010.094 ±  16.045  ns/op
FastPrintfBenchmark.fastPrintfWithThreadLocal  avgt    6   998.360 ±  51.938  ns/op
FastPrintfBenchmark.jdkPrintf                  avgt    6  4075.616 ± 363.936  ns/op
 *     </pre>
 */
public class FastPrintfBenchmark {

  private static final String FORMAT =
      "This is plain text: % d %% %-8X %% %#08o %% %.3f %% %-7.5g %% %+10.5e";
  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(FORMAT);
  private static final FastPrintf FAST_PRINTF2 = FAST_PRINTF.enableThreadLocalCache();

  private Long v1;
  private Long v2;
  private Long v3;
  private Double d1;
  private Double d2;
  private Double d3;

  @Setup(Level.Invocation)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    v1 = random.nextLong();
    v2 = random.nextLong();
    v3 = random.nextLong();
    d1 = random.nextDouble() * 1e6;
    d2 = random.nextDouble() * 1e6;
    d3 = random.nextDouble() * 1e6;
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
