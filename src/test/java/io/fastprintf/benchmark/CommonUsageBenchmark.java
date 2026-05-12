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
 * <p>JDK 21 (Corretto 21.0.9), JMH 1.37, @Fork(2):
 *
 * <pre>
 * Benchmark                                   Mode  Cnt    Score    Error  Units
 * CommonUsageBenchmark.fastPrintfArgs         avgt    6  245.779 ± 11.235  ns/op
 * CommonUsageBenchmark.fastPrintfThreadLocal  avgt    6  226.355 ± 13.692  ns/op
 * CommonUsageBenchmark.fastPrintfVarargs      avgt    6  226.687 ± 26.312  ns/op
 * CommonUsageBenchmark.jdkPrintf              avgt    6  488.313 ± 51.671  ns/op
 * </pre>
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

  private String level;
  private String user;
  private int id;
  private double latency;

  @Setup(Level.Invocation)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    level = LEVELS[random.nextInt(LEVELS.length)];
    user = USERS[random.nextInt(USERS.length)];
    id = random.nextInt(1, 100_000);
    latency = random.nextDouble() * 1000.0;
  }

  @Benchmark
  public String fastPrintfVarargs() {
    return FAST_PRINTF.format(level, user, id, latency);
  }

  @Benchmark
  public String fastPrintfArgs() {
    Args args =
        Args.createWithExpectedSize(4)
            .putString(level)
            .putString(user)
            .putInt(id)
            .putDouble(latency);
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfThreadLocal() {
    return FAST_PRINTF_TL.format(level, user, id, latency);
  }

  @Benchmark
  public String jdkPrintf() {
    return String.format(FORMAT, level, user, id, latency);
  }
}
