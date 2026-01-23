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
/**
 *
 *
 * <pre>
 * Benchmark                                Mode  Cnt    Score    Error  Units
 * LongJoinBenchmark.fastPrintf             avgt    6  385.487 ± 58.624  ns/op
 * LongJoinBenchmark.fastPrintfWithBuilder  avgt    6  390.356 ±  6.843  ns/op
 * LongJoinBenchmark.jdkStringFormat        avgt    6  524.620 ± 21.966  ns/op
 * LongJoinBenchmark.stringJoin             avgt    6  186.626 ±  2.906  ns/op
 * </pre>
 */
public class LongJoinBenchmark {

  private static final String FORMAT_STRING = "%s, %s, %s, %s, %s, %s";
  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(FORMAT_STRING);
  private long[] ls;

  @Setup(Level.Invocation)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    ls = random.longs(6).toArray();
  }

  @Benchmark
  public String stringJoin() {
    long[] args = this.ls;
    StringBuilder builder = new StringBuilder(args.length * 16);
    builder.append(args[0]);
    for (int i = 1; i < args.length; ++i) {
      builder.append(", ").append(args[i]);
    }
    return builder.toString();
  }

  @Benchmark
  public String fastPrintf() {
    Args args = Args.of(ls[0], ls[1], ls[2], ls[3], ls[4], ls[5]);
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String fastPrintfWithBuilder() {
    Args args =
        Args.createWithExpectedSize(ls.length)
            .putLong(ls[0])
            .putLong(ls[1])
            .putLong(ls[2])
            .putLong(ls[3])
            .putLong(ls[4])
            .putLong(ls[5]);
    return FAST_PRINTF.format(args);
  }

  @Benchmark
  public String jdkStringFormat() {
    return String.format(FORMAT_STRING, ls[0], ls[1], ls[2], ls[3], ls[4], ls[5]);
  }
}
