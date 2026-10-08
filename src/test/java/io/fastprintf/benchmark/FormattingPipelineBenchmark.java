package io.fastprintf.benchmark;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.math.BigDecimal;
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

/** Isolates formatting costs using reproducible, pre-generated argument pools. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class FormattingPipelineBenchmark {
  @Param({
    "integer",
    "paddedInteger",
    "dynamic",
    "upperHex",
    "fixed",
    "scientific",
    "general",
    "bigDecimal"
  })
  public String workload;

  private FastPrintf formatter;
  private Args[] arguments;
  private int index;

  @Setup
  public void setup() {
    String pattern;
    switch (workload) {
      case "integer":
        pattern = "%d";
        break;
      case "paddedInteger":
        pattern = "%+016.10d";
        break;
      case "dynamic":
        pattern = "%*.*f";
        break;
      case "upperHex":
        pattern = "%#016X";
        break;
      case "fixed":
        pattern = "%+.3f";
        break;
      case "scientific":
        pattern = "%+#18.6E";
        break;
      case "general":
        pattern = "%#.6G";
        break;
      case "bigDecimal":
        pattern = "%#.6g";
        break;
      default:
        throw new IllegalArgumentException(workload);
    }
    formatter = FastPrintf.compile(pattern);
    arguments = new Args[1024];
    Random random = new Random(20261008L);
    for (int i = 0; i < arguments.length; i++) {
      long integer = random.nextLong();
      double value = (random.nextDouble() - 0.5) * Math.pow(10, (i % 21) - 10);
      switch (workload) {
        case "integer":
        case "paddedInteger":
        case "upperHex":
          arguments[i] = Args.create().putLong(integer);
          break;
        case "dynamic":
          arguments[i] =
              Args.create().putInt((i & 1) == 0 ? 18 : -18).putInt(i % 7).putDouble(value);
          break;
        case "bigDecimal":
          arguments[i] = Args.create().putBigDecimal(BigDecimal.valueOf(value));
          break;
        default:
          arguments[i] = Args.create().putDouble(value);
      }
    }
  }

  @Benchmark
  public String format() {
    index = (index + 1) & (arguments.length - 1);
    return formatter.format(arguments[index]);
  }
}
