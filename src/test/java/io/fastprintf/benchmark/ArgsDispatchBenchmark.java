package io.fastprintf.benchmark;

import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/**
 * Separates argument construction from formatting, with identical capacity and input pools. Boxed
 * paths preserve the same references; the primitive path intentionally avoids boxing and preserves
 * primitive argument semantics. The mixed loop hides concrete input types from its call site,
 * unlike the individually typed array loads in {@code runtimeBoxed}.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 4, time = 1)
@Fork(3)
public class ArgsDispatchBenchmark {
  private final FastPrintf formatter = FastPrintf.compile("[%s] %s id=%d latency=%.3fms");
  private final String[] levels = new String[1024];
  private final String[] users = new String[1024];
  private final int[] ids = new int[1024];
  private final double[] latencies = new double[1024];
  private final Integer[] boxedIds = new Integer[1024];
  private final Double[] boxedLatencies = new Double[1024];
  private final Object[][] mixed = new Object[1024][];
  private int index;

  @Setup
  public void setup() {
    Random random = new Random(42);
    for (int i = 0; i < ids.length; i++) {
      levels[i] = (i & 1) == 0 ? "INFO" : "WARN";
      users[i] = "user-" + i;
      ids[i] = 1000 + random.nextInt(99000);
      latencies[i] = random.nextDouble() * 1000;
      boxedIds[i] = ids[i];
      boxedLatencies[i] = latencies[i];
      mixed[i] = new Object[] {levels[i], users[i], boxedIds[i], boxedLatencies[i]};
      String expected = formatter.format(typedPrimitive(i));
      if (!expected.equals(formatter.format(runtimeBoxed(i)))
          || !expected.equals(formatter.format(typedBoxed(i)))
          || !expected.equals(formatter.format(runtimeMixed(i)))
          || !expected.equals(formatter.format(varargs(i)))) {
        throw new AssertionError("Different formatted values at " + i);
      }
    }
  }

  private int next() {
    index = (index + 1) & 1023;
    return index;
  }

  private Args runtimeBoxed(int i) {
    return Args.createWithExpectedSize(4)
        .put(levels[i])
        .put(users[i])
        .put(boxedIds[i])
        .put(boxedLatencies[i]);
  }

  private Args typedBoxed(int i) {
    return Args.createWithExpectedSize(4)
        .putString(levels[i])
        .putString(users[i])
        .putIntOrNull(boxedIds[i])
        .putDoubleOrNull(boxedLatencies[i]);
  }

  private Args runtimeMixed(int i) {
    Args args = Args.createWithExpectedSize(4);
    for (Object value : mixed[i]) {
      args.put(value);
    }
    return args;
  }

  private Args typedPrimitive(int i) {
    return Args.createWithExpectedSize(4)
        .putString(levels[i])
        .putString(users[i])
        .putInt(ids[i])
        .putDouble(latencies[i]);
  }

  private Args varargs(int i) {
    return Args.of(levels[i], users[i], ids[i], latencies[i]);
  }

  @Benchmark
  public Args buildRuntimeBoxed() {
    return runtimeBoxed(next());
  }

  @Benchmark
  public Args buildTypedBoxed() {
    return typedBoxed(next());
  }

  @Benchmark
  public Args buildRuntimeMixed() {
    return runtimeMixed(next());
  }

  @Benchmark
  public Args buildTypedPrimitive() {
    return typedPrimitive(next());
  }

  @Benchmark
  public Args buildVarargs() {
    return varargs(next());
  }

  @Benchmark
  public String formatRuntimeBoxed() {
    return formatter.format(runtimeBoxed(next()));
  }

  @Benchmark
  public String formatTypedBoxed() {
    return formatter.format(typedBoxed(next()));
  }

  @Benchmark
  public String formatRuntimeMixed() {
    return formatter.format(runtimeMixed(next()));
  }

  @Benchmark
  public String formatTypedPrimitive() {
    return formatter.format(typedPrimitive(next()));
  }

  @Benchmark
  public String formatVarargs() {
    return formatter.format(varargs(next()));
  }
}
