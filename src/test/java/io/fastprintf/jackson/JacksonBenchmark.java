package io.fastprintf.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.fastprintf.Args;
import io.fastprintf.FastPrintf;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/** Compares automatic adaptation with manual extraction and reusable argument adapters. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 4, time = 1)
@Fork(2)
public class JacksonBenchmark {
  private FastPrintf formatter;
  private JsonNode[] names;
  private JsonNode[] amounts;
  private Args[] prepared;
  private int index;

  @Setup
  public void setup() {
    formatter = FastPrintf.compile("%s: %.2f");
    names = new JsonNode[1024];
    amounts = new JsonNode[1024];
    prepared = new Args[1024];
    for (int i = 0; i < names.length; i++) {
      names[i] = TextNode.valueOf("name-" + i);
      amounts[i] = DecimalNode.valueOf(BigDecimal.valueOf(i * 1234567L, 3));
      prepared[i] = Args.of(names[i], amounts[i]);
    }
  }

  @Benchmark
  public String automaticNodes() {
    int i = index++ & 1023;
    return formatter.format(names[i], amounts[i]);
  }

  @Benchmark
  public String manualExtraction() {
    int i = index++ & 1023;
    return formatter.format(names[i].textValue(), amounts[i].decimalValue());
  }

  @Benchmark
  public String preparedNodes() {
    return formatter.format(prepared[index++ & 1023]);
  }
}
