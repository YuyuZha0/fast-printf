package io.fastprintf.benchmark;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/** Varies the number of formatter strategies seen at the shared dispatch call site. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 4, time = 1)
@Fork(2)
public class DispatchBenchmark {
  @Param({"1", "2", "4", "8"})
  public int specifiers;

  private FastPrintf formatter;
  private Args[] arguments;
  private int index;

  @Setup
  public void setup() {
    String[] conversions = {"%d", "%.3f", "%s", "%#x", "%c", "%.2e", "%.4g", "%S"};
    StringBuilder pattern = new StringBuilder();
    for (int i = 0; i < specifiers; i++) {
      if (i > 0) pattern.append('|');
      pattern.append(conversions[i]);
    }
    formatter = FastPrintf.compile(pattern.toString());
    Random random = new Random(42);
    arguments = new Args[1024];
    for (int i = 0; i < arguments.length; i++) {
      Object[] values = {
        random.nextLong(),
        random.nextDouble() * 1000,
        "message-" + i,
        random.nextInt(),
        (char) ('a' + i % 26),
        random.nextDouble(),
        random.nextDouble(),
        "mixed"
      };
      Args args = Args.createWithExpectedSize(specifiers);
      for (int j = 0; j < specifiers; j++) args.put(values[j]);
      arguments[i] = args;
    }
  }

  @Benchmark
  public String format() {
    index = (index + 1) & (arguments.length - 1);
    return formatter.format(arguments[index]);
  }
}
