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

/**
 * Apples-to-apples benchmark for the most common real-world printf usage: a short log-line-style
 * format mixing literal text with {@code %s}, {@code %d}, and {@code %.Nf}. The format string is
 * accepted byte-for-byte by both {@code FastPrintf} and {@link String#format(String, Object...)},
 * so no per-side translation hacks are needed.
 *
 * <p>All argument values are pre-generated into pools in {@code @Setup(Level.Trial)} and each
 * benchmark rotates through them by index. This deliberately avoids per-invocation allocation
 * or random/math work, which {@link ComplexFormatLocalityBenchmark} shows can cold-cache the
 * {@code enableThreadLocalCache()} path's reused {@code StringBuilder.value} buffer and distort
 * the comparison.
 *
 * <p>JDK 21 (Corretto 21.0.9), JMH 1.37, @Fork(2):
 *
 * <pre>
 * Benchmark                                   Mode  Cnt    Score    Error  Units
 * CommonUsageBenchmark.fastPrintfArgs         avgt    6  247.849 ± 18.696  ns/op
 * CommonUsageBenchmark.fastPrintfThreadLocal  avgt    6  187.436 ±  9.990  ns/op
 * CommonUsageBenchmark.fastPrintfVarargs      avgt    6  229.585 ± 96.210  ns/op
 * CommonUsageBenchmark.jdkPrintf              avgt    6  404.211 ± 21.528  ns/op
 * </pre>
 *
 * <p>Allocation profile ({@code -prof gc}, {@code gc.alloc.rate.norm} = B/op):
 *
 * <pre>
 * fastPrintfArgs         592 B/op   (no boxing of primitives)
 * fastPrintfThreadLocal  608 B/op   (boxed varargs, reuses cached StringBuilder)
 * fastPrintfVarargs      696 B/op   (boxed varargs + fresh StringBuilder)
 * jdkPrintf             1280 B/op   (~2.1× the allocation of fastPrintf varargs)
 * </pre>
 *
 * <p><b>Caveat on the {@code fastPrintfThreadLocal} number.</b> The pooled-args setup keeps the
 * reused {@code StringBuilder.value} cache-hot between invocations, which is the best case for
 * {@code enableThreadLocalCache()}. Workloads that allocate or touch unrelated memory between
 * {@code format()} calls cold-cache that buffer; under those conditions the TL path can match
 * or fall behind the non-TL path. See {@link ComplexFormatLocalityBenchmark} for the full
 * locality analysis.
 */
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 5)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
public class CommonUsageBenchmark {

  private static final String FORMAT = "[%s] %s id=%d latency=%.3fms";

  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(FORMAT);
  private static final FastPrintf FAST_PRINTF_TL = FAST_PRINTF.enableThreadLocalCache();

  private static final String[] LEVELS = {"INFO", "WARN", "ERROR", "DEBUG"};
  private static final String[] USERS = {"alice", "bob", "carol", "dave"};

  // Power-of-two pool size so we can rotate with a cheap AND mask.
  private static final int POOL_SIZE = 1024;
  private static final int POOL_MASK = POOL_SIZE - 1;

  private String[] levelPool;
  private String[] userPool;
  private int[] idPool;
  private double[] latencyPool;
  private int index;

  @Setup(Level.Trial)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    levelPool = new String[POOL_SIZE];
    userPool = new String[POOL_SIZE];
    idPool = new int[POOL_SIZE];
    latencyPool = new double[POOL_SIZE];
    for (int i = 0; i < POOL_SIZE; i++) {
      levelPool[i] = LEVELS[random.nextInt(LEVELS.length)];
      userPool[i] = USERS[random.nextInt(USERS.length)];
      idPool[i] = random.nextInt(1, 100_000);
      latencyPool[i] = random.nextDouble() * 1000.0;
    }
  }

  @Benchmark
  public String fastPrintfVarargs() {
    int i = index;
    index = (index + 1) & POOL_MASK;
    return FAST_PRINTF.format(levelPool[i], userPool[i], idPool[i], latencyPool[i]);
  }

  @Benchmark
  public String fastPrintfArgs() {
    int i = index;
    index = (index + 1) & POOL_MASK;
    Args args =
        Args.createWithExpectedSize(4)
            .putString(levelPool[i])
            .putString(userPool[i])
            .putInt(idPool[i])
            .putDouble(latencyPool[i]);
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfThreadLocal() {
    int i = index;
    index = (index + 1) & POOL_MASK;
    return FAST_PRINTF_TL.format(levelPool[i], userPool[i], idPool[i], latencyPool[i]);
  }

  @Benchmark
  public String jdkPrintf() {
    int i = index;
    index = (index + 1) & POOL_MASK;
    return String.format(FORMAT, levelPool[i], userPool[i], idPool[i], latencyPool[i]);
  }
}