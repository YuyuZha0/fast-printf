package io.fastprintf.benchmark;

import io.fastprintf.FastPrintf;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 5)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
/*
* This benchmark tests a format string with a high density of complex specifiers
* and a diverse set of argument types. It is designed to stress the argument
* handling and formatting logic rather than raw string appending speed.
*
* <pre>
Benchmark                                         Mode  Cnt     Score    Error  Units
ComplexFormatBenchmark.fastPrintf                 avgt    6   379.913 ± 25.926  ns/op
ComplexFormatBenchmark.fastPrintfWithThreadLocal  avgt    6   454.122 ± 32.834  ns/op
ComplexFormatBenchmark.jdkPrintf                  avgt    6  1235.600 ± 23.107  ns/op
* </pre>
*/
public class ComplexFormatBenchmark {

  // A format string with many complex specifiers and minimal literal text.
  // Format string for fast-printf, testing its native %S implementation.
  private static final String COMPLEX_FORMAT = "%#018x|%-15.7g|%S|%c|%d|%15.5f";
  private static final String JDK_COMPLEX_FORMAT = "%#018x|%-15.7g|%s|%c|%d|%15.5f";

  private static final FastPrintf FAST_PRINTF = FastPrintf.compile(COMPLEX_FORMAT);
  private static final FastPrintf FAST_PRINTF_THREADED = FAST_PRINTF.enableThreadLocalCache();

  private long longValue;
  private double doubleValue;
  private String stringValue;
  private char charValue;
  private boolean boolValue;
  private BigDecimal bigDecimalValue;

  @Setup(Level.Invocation)
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    longValue = random.nextLong();
    doubleValue = random.nextDouble() * 1e6;
    stringValue = "JMH-Benchmark-String";
    charValue = (char) ('A' + random.nextInt(26));
    boolValue = random.nextBoolean();
    bigDecimalValue = BigDecimal.valueOf(random.nextDouble() * 1e-6);
  }

  @Benchmark
  public String fastPrintf() {
    return FAST_PRINTF.format(
        longValue, doubleValue, stringValue, charValue, boolValue, bigDecimalValue);
  }

  @Benchmark
  public String fastPrintfWithThreadLocal() {
    return FAST_PRINTF_THREADED.format(
        longValue, doubleValue, stringValue, charValue, boolValue, bigDecimalValue);
  }

  @Benchmark
  public String jdkPrintf() {
    // String.format does not support BigDecimal directly in this context without
    // using a Formattable interface. We use a double for a comparable (though not identical)
    // workload.
    return String.format(
        JDK_COMPLEX_FORMAT,
        longValue,
        doubleValue,
        stringValue,
        charValue,
        boolValue ? 1 : 0,
        bigDecimalValue.doubleValue() // Using double for jdkPrintf
        );
  }
}
