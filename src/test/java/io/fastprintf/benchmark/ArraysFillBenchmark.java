package io.fastprintf.benchmark;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 *
 *
 * <pre>
 *     Benchmark                               (length)  Mode  Cnt    Score    Error  Units
 * ArraysFillBenchmark.arrayFillAndAppend         4  avgt   10   22.445 ±  1.051  ns/op
 * ArraysFillBenchmark.arrayFillAndAppend         8  avgt   10   23.332 ±  1.167  ns/op
 * ArraysFillBenchmark.arrayFillAndAppend        16  avgt   10   26.373 ±  1.108  ns/op
 * ArraysFillBenchmark.arrayFillAndAppend        32  avgt   10   36.679 ±  1.283  ns/op
 * ArraysFillBenchmark.arrayFillAndAppend        64  avgt   10   31.424 ±  2.180  ns/op
 * ArraysFillBenchmark.arrayFillAndAppend       128  avgt   10   48.966 ±  1.001  ns/op
 * ArraysFillBenchmark.arrayFillAndAppend       256  avgt   10   94.074 ±  1.874  ns/op
 * ArraysFillBenchmark.loopFill                   4  avgt   10   15.402 ±  0.278  ns/op
 * ArraysFillBenchmark.loopFill                   8  avgt   10   20.065 ±  0.034  ns/op
 * ArraysFillBenchmark.loopFill                  16  avgt   10   41.813 ±  0.070  ns/op
 * ArraysFillBenchmark.loopFill                  32  avgt   10   75.992 ±  0.399  ns/op
 * ArraysFillBenchmark.loopFill                  64  avgt   10  143.713 ±  1.141  ns/op
 * ArraysFillBenchmark.loopFill                 128  avgt   10  278.330 ±  3.433  ns/op
 * ArraysFillBenchmark.loopFill                 256  avgt   10  555.070 ± 10.591  ns/op
 *     </pre>
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2, jvmArgsAppend = "-Xmx1g")
public class ArraysFillBenchmark {

  @Param({"4", "8", "16", "32", "64", "128", "256"})
  private int length;

  // The character to be used for the current invocation.
  private char ch;

  // A pool of random characters to cycle through.
  private char[] charPool;
  private int charPoolIndex = 0;

  // The StringBuilder is created once per trial.
  private StringBuilder builder;

  @Setup(Level.Trial)
  public void setupTrial() {
    // Create the StringBuilder once with a large enough capacity.
    int capacity = "PREFIX:".length() + length;
    builder = new StringBuilder(capacity);

    // A pool of 128 is more than enough to ensure variability without being too large.
    charPool = new char[128];
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < charPool.length; i++) {
      charPool[i] = (char) random.nextInt(32, 127); // Printable ASCII
    }
  }

  @Setup(Level.Invocation)
  public void setupInvocation() {
    // Reset the StringBuilder for each call.
    builder.setLength(0);
    builder.append("PREFIX:");

    ch = charPool[charPoolIndex];
    charPoolIndex = (charPoolIndex + 1) % charPool.length;
  }

  @Benchmark
  public void loopFill(Blackhole bh) {
    for (int i = 0; i < length; i++) {
      builder.append(ch);
    }
    bh.consume(builder);
  }

  @Benchmark
  public void arrayFillAndAppend(Blackhole bh) {
    char[] fill = new char[length];
    Arrays.fill(fill, ch);
    builder.append(fill);
    bh.consume(builder);
  }
}
