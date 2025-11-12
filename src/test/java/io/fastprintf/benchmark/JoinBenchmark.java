package io.fastprintf.benchmark;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.util.Base64;
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
/**
 *
 *
 * <pre>
 * Benchmark                                Mode  Cnt    Score    Error  Units
 * JoinBenchmark.fastPrintf                 avgt    6  349.548 ±  9.540  ns/op
 * JoinBenchmark.fastPrintfWithBuilder      avgt    6  365.438 ± 33.032  ns/op
 * JoinBenchmark.fastPrintfWithThreadLocal  avgt    6  309.684 ± 40.986  ns/op
 * JoinBenchmark.jdkStringFormat            avgt    6  449.457 ±  5.022  ns/op
 * JoinBenchmark.stringJoin                 avgt    6  292.194 ±  2.453  ns/op
 * </pre>
 */
public class JoinBenchmark {

  private static final String FORMAT_STRING = "%s, %s, %s, %s, %s, %s";
  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(FORMAT_STRING);
  private static final FastPrintf FAST_PRINTF2 = FAST_PRINTF.enableThreadLocalCache();
  private Object[] objects;

  @Setup(Level.Invocation)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    byte[] randomBytes = new byte[12];
    random.nextBytes(randomBytes);
    objects =
        new Object[] {
          random.nextInt(),
          random.nextLong(),
          random.nextDouble() * 1E6,
          Base64.getEncoder().encodeToString(randomBytes),
          random.nextBoolean(),
          null
        };
  }

  @Benchmark
  public String stringJoin() {
    Object[] args = this.objects;
    StringBuilder builder = new StringBuilder(args.length * 16);
    builder.append(args[0]);
    for (int i = 1; i < args.length; ++i) {
      builder.append(", ").append(args[i]);
    }
    return builder.toString();
  }

  @Benchmark
  public String fastPrintf() {
    Args args = Args.of(objects);
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfWithBuilder() {
    Args args =
        Args.createWithExpectedSize(objects.length)
            .putInt((Integer) objects[0])
            .putLong((Long) objects[1])
            .putDouble((Double) objects[2])
            .putString((String) objects[3])
            .putBoolean((Boolean) objects[4])
            .putNull();
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    Args args = Args.of(objects);
    return FAST_PRINTF2.format(args);
  }

  @Benchmark
  public String jdkStringFormat() {
    return String.format(FORMAT_STRING, objects);
  }
}
