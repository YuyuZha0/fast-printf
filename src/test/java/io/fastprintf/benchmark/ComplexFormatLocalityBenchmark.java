package io.fastprintf.benchmark;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.math.BigDecimal;
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
 * Companion to {@link ComplexFormatBenchmark} that isolates why {@code enableThreadLocalCache()}
 * appears either as a win or a regression depending on the surrounding workload.
 *
 * <h2>Why this benchmark exists</h2>
 *
 * {@link ComplexFormatBenchmark} consistently shows {@code fastPrintfWithThreadLocal} running
 * ~90 ns slower per op than {@code fastPrintf}, despite allocating fewer bytes. The intuitive
 * stories — "the TL lambda has per-call overhead", "TL saves a StringBuilder regrowth",
 * "TL reduces GC pressure" — all turn out to be wrong when probed:
 *
 * <ul>
 *   <li>Direct measurement of the two factory lambdas in isolation: identical (~4 ns each).
 *   <li>Pre-sizing the StringBuilder (no TL) to skip the regrowth: no measurable improvement
 *       — the regrowth saving is within JMH's noise on this workload.
 *   <li>{@code -prof gc}: the TL path allocates ~225 B/op less yet runs slower. So it isn't
 *       about allocation cost.
 * </ul>
 *
 * <p>The only structural difference left between the two paths is <b>where the
 * {@code StringBuilder.value} char[] lives in cache</b>. The non-TL path allocates a fresh
 * {@code char[]} in TLAB and writes into it while it is still cache-hot from the allocator
 * having just zeroed it. The TL path reuses a long-lived heap {@code char[]} from a previous
 * invocation; if anything ran in between that touched unrelated memory, that {@code char[]}
 * has been evicted from L1/L2 and the format work pays cache-miss penalties when it starts
 * writing into it. A ~94-byte buffer spans two cache lines, and two L3/main-memory fetches
 * at ~30–100 ns each lands squarely in the ~90 ns gap observed.
 *
 * <h2>The experiment</h2>
 *
 * Same format string and arguments as {@link ComplexFormatBenchmark}, but all argument values
 * are pre-generated into a pool in {@code @Setup(Level.Trial)}. Each {@code @Benchmark} method
 * just indexes into the pool — no random calls, no {@code BigDecimal.valueOf}, no boxing per
 * invocation. If cache locality is the dominant factor, removing the between-invocation
 * allocation churn should keep the cached {@code char[]} cache-hot, and the TL gap should
 * disappear.
 *
 * <h2>Result (1 fork × 5 iter × 1 s, JDK 17, M-series CPU)</h2>
 *
 * <pre>
 * Benchmark                                                       Mode  Cnt    Score    Error   Units
 * ComplexFormatLocalityBenchmark.fastPrintf                       avgt   10  339.276 ± 61.354   ns/op
 * ComplexFormatLocalityBenchmark.fastPrintfWithThreadLocal        avgt   10  327.157 ±  6.670   ns/op
 *
 * (gc.alloc.rate.norm — bytes allocated per op)
 * ComplexFormatLocalityBenchmark.fastPrintf                       avgt   10  1079 B/op
 * ComplexFormatLocalityBenchmark.fastPrintfWithThreadLocal        avgt   10   915 B/op
 * </pre>
 *
 * Compared head-to-head with {@link ComplexFormatBenchmark} (allocation-heavy per-invocation
 * setup):
 *
 * <pre>
 *                                         non-TL      TL       gap
 *   Original (BigDecimal etc. per inv)    365 ns    457 ns    TL +92 ns slower
 *   Pooled   (no allocation per inv)      339 ns    327 ns    TL ~tied / slightly faster
 * </pre>
 *
 * <h2>Findings</h2>
 *
 * <ol>
 *   <li><b>The 90 ns regression in {@link ComplexFormatBenchmark} is caused by cache locality,
 *       not by anything intrinsic to {@code enableThreadLocalCache()}.</b> Remove the
 *       per-invocation allocation churn and the gap vanishes (and even slightly inverts).
 *
 *   <li><b>{@code enableThreadLocalCache()} is a bet on locality, not on saving allocation or
 *       regrowth.</b> The TL path's only real advantage on the JMH hot loop is keeping the
 *       same {@code char[]} hot across calls; TLAB allocation is otherwise nearly free. The
 *       moment the caller does meaningful work between {@code format()} calls (allocations,
 *       random data, log context, anything that touches memory), that cached {@code char[]}
 *       goes cold and the TL path starts losing on cache misses.
 *
 *   <li><b>Production code looks more like {@link ComplexFormatBenchmark} than like this
 *       benchmark.</b> Real callers virtually always do some allocation/work between
 *       {@code format()} invocations — building log records, generating IDs, capturing
 *       timestamps. So in practice, {@code enableThreadLocalCache()} is more likely to be
 *       the ~90 ns regression of the original benchmark than the ~12 ns improvement seen
 *       here.
 *
 *   <li><b>The {@code JoinBenchmark} "TL wins by 50 ns" result reflects the lighter
 *       per-invocation setup of that benchmark</b> (no {@code BigDecimal}, simpler math),
 *       which sits closer to the pooled regime than to the heavy regime. It overstates the
 *       benefit you should expect in real code.
 *
 *   <li><b>If you reach for an SB-reuse optimisation, prefer caller-provided {@code Appendable}
 *       APIs over a private {@code ThreadLocal}.</b> When the caller controls the buffer's
 *       lifetime, they also control its locality — they can keep it hot in their own working
 *       set instead of relying on the library's cached copy surviving cache pressure.
 * </ol>
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2, jvmArgsAppend = "-Xmx1g")
public class ComplexFormatLocalityBenchmark {

  private static final String COMPLEX_FORMAT = "%#018x|%-15.7g|%S|%c|%d|%15.5f";
  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(COMPLEX_FORMAT);
  private static final FastPrintf FAST_PRINTF_THREADED = FAST_PRINTF.enableThreadLocalCache();

  // Power-of-two pool size so we can rotate with a cheap AND mask.
  private static final int POOL_SIZE = 1024;
  private static final int POOL_MASK = POOL_SIZE - 1;

  // Pre-built Args instances — eliminates per-invocation boxing and BigDecimal allocation,
  // which is the variable being tested vs ComplexFormatBenchmark's per-invocation setup.
  private Args[] argsPool;
  private int index;

  @Setup(Level.Trial)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    argsPool = new Args[POOL_SIZE];
    for (int i = 0; i < POOL_SIZE; i++) {
      long longValue = random.nextLong();
      double doubleValue = random.nextDouble() * 1e6;
      String stringValue = "JMH-Benchmark-String";
      char charValue = (char) ('A' + random.nextInt(26));
      boolean boolValue = random.nextBoolean();
      BigDecimal bigDecimalValue = BigDecimal.valueOf(random.nextDouble() * 1e-6);
      argsPool[i] =
          Args.of(longValue, doubleValue, stringValue, charValue, boolValue, bigDecimalValue);
    }
  }

  @Benchmark
  public String fastPrintf() {
    int i = index;
    index = (index + 1) & POOL_MASK;
    return FAST_PRINTF.format(argsPool[i]);
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    int i = index;
    index = (index + 1) & POOL_MASK;
    return FAST_PRINTF_THREADED.format(argsPool[i]);
  }
}