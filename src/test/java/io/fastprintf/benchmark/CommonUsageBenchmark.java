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
 * <p>Cross-JDK results (Corretto 8.0.472, 11.0.29, 21.0.9 on the same M-series box), JMH 1.37,
 * {@code @Fork(2)}:
 *
 * <pre>
 * Path                                     JDK 8 (ns)    JDK 11 (ns)   JDK 21 (ns)
 * --------------------------------------------------------------------------------
 * fastPrintf (varargs)                     214.33 ±21.4  215.65 ±28.1  221.21 ±77.4
 * fastPrintf (Args builder, no-boxing)     326.35 ±14.5  250.92 ± 9.2  246.75 ±25.8
 * fastPrintf (with enableThreadLocalCache) 243.97 ±15.5  198.80 ±14.5  186.11 ± 3.0
 * String.format (jdkPrintf)               1450.11 ±25.4 1069.47 ±24.4  404.46 ±18.1
 *
 * Speedup vs String.format:
 *   varargs                                  6.77×         4.95×         1.83×
 *   enableThreadLocalCache                   5.94×         5.38×         2.17×
 * </pre>
 *
 * <p>Allocation profile (JDK 21, {@code -prof gc}, {@code gc.alloc.rate.norm} = B/op; JMH's
 * GC profiler does not produce reliable B/op numbers on Hotspot 8, so allocation data is
 * reported for JDK 21 only):
 *
 * <pre>
 * fastPrintfArgs         592 B/op   (no boxing of primitives)
 * fastPrintfThreadLocal  608 B/op   (boxed varargs, reuses cached StringBuilder)
 * fastPrintfVarargs      696 B/op   (boxed varargs + fresh StringBuilder)
 * jdkPrintf             1280 B/op   (~2.1× the allocation of fastPrintf varargs)
 * </pre>
 *
 * <h2>What the cross-JDK regression tells us</h2>
 *
 * <ul>
 *   <li><b>fast-printf is JDK-version-invariant on the varargs path</b> (214 → 216 → 221 ns).
 *       The library manages its own performance; it does not depend on Hotspot improvements
 *       that landed in JDK 17+.
 *   <li><b>The {@code Args} no-boxing builder gets meaningfully faster on JDK 11+</b>
 *       (326 → 251 ns). The chained small-method calls benefit from JIT inlining improvements
 *       in newer Hotspots; on JDK 8 it is the slowest fast-printf path, on JDK 11+ it costs
 *       only ~30 ns extra vs varargs.
 *   <li><b>{@code enableThreadLocalCache()} improves monotonically</b> (244 → 199 → 186 ns).
 *   <li><b>{@code String.format} is the big mover.</b> JDK 21's rewritten Formatter is 3.6×
 *       faster than JDK 8's. That is why fast-printf's relative advantage shrinks from ~6.8×
 *       on JDK 8 to ~1.8× on JDK 21 — not because fast-printf got slower (it didn't), but
 *       because the JDK got dramatically better.
 * </ul>
 *
 * <p>Practical takeaway: fast-printf is most valuable on older JDKs (8 / 11). On JDK 21
 * the speedup is more modest, and the allocation reduction (~50% less garbage than
 * {@code String.format}) becomes the main reason to use it.
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