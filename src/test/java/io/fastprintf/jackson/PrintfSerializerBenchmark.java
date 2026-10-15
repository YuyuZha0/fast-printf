package io.fastprintf.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fastprintf.FastPrintf;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/** Compares steady-state annotation serialization with a hand-written compiled serializer. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 4, time = 1)
@Fork(2)
public class PrintfSerializerBenchmark {
  public static class Price {
    @PrintfFormat("%.2f")
    public BigDecimal amount;
  }

  public abstract static class ManualMixin {
    @JsonSerialize(using = ManualSerializer.class)
    public BigDecimal amount;
  }

  public abstract static class ConfiguredMixin {
    @PrintfFormat(value = "%.2f", enableThreadLocalCache = true, stringBuilderInitialCapacity = 64)
    public BigDecimal amount;
  }

  public static class ManualSerializer extends JsonSerializer<BigDecimal> {
    private static final FastPrintf FORMAT = FastPrintf.compile("%.2f");

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(FORMAT.format(value));
    }
  }

  private ObjectWriter module;
  private ObjectWriter manual;
  private ObjectWriter configured;
  private Price[] values;
  private int index;

  @Setup
  public void setup() throws IOException {
    module = new ObjectMapper().registerModule(new FastPrintfModule()).writerFor(Price.class);
    manual = new ObjectMapper().addMixIn(Price.class, ManualMixin.class).writerFor(Price.class);
    configured =
        new ObjectMapper()
            .registerModule(new FastPrintfModule())
            .addMixIn(Price.class, ConfiguredMixin.class)
            .writerFor(Price.class);
    values = new Price[1024];
    for (int i = 0; i < values.length; i++) {
      Price value = new Price();
      value.amount = BigDecimal.valueOf(i * 1234567L, 3);
      values[i] = value;
      String expected = manual.writeValueAsString(value);
      if (!expected.equals(module.writeValueAsString(value))
          || !expected.equals(configured.writeValueAsString(value))) {
        throw new AssertionError("Different JSON at " + i);
      }
    }
  }

  @Benchmark
  public String annotationWithModule() throws IOException {
    return module.writeValueAsString(values[index++ & 1023]);
  }

  @Benchmark
  public String configuredAnnotation() throws IOException {
    return configured.writeValueAsString(values[index++ & 1023]);
  }

  @Benchmark
  public String manualSerializer() throws IOException {
    return manual.writeValueAsString(values[index++ & 1023]);
  }
}
