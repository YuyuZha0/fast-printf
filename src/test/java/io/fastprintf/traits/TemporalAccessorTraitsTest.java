package io.fastprintf.traits;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class TemporalAccessorTraitsTest {

  @Test
  public void testLocalDateTime() {
    LocalDateTime now = LocalDateTime.now();
    TemporalAccessorTraits traits = new TemporalAccessorTraits(now);

    assertEquals(now, traits.asTemporalAccessor());
    assertEquals(now.toString(), traits.asString());
  }

  @Test
  public void testInstant() {
    Instant now = Instant.now();
    TemporalAccessorTraits traits = new TemporalAccessorTraits(now);

    assertEquals(now, traits.asTemporalAccessor());
    assertEquals(now.toString(), traits.asString());

    assertEquals(now.getEpochSecond(), traits.asInt());
    assertEquals(Long.toHexString(now.toEpochMilli()), traits.asIntForm().toHexString());

    assertEquals(
        now.getEpochSecond() + now.getNano() / 1e9,
        Double.parseDouble(traits.asFloatForm().toString()),
        0.0000000001);
  }

  @Test
  public void testOffsetDateTime() {
    Instant now = Instant.now();
    OffsetDateTime nowOffset = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
    TemporalAccessorTraits traits = new TemporalAccessorTraits(nowOffset);

    assertEquals(nowOffset, traits.asTemporalAccessor());
    assertEquals(nowOffset.toString(), traits.asString());

    assertEquals(now.getEpochSecond(), traits.asInt());
    assertEquals(Long.toHexString(now.toEpochMilli()), traits.asIntForm().toHexString());

    assertEquals(
        now.getEpochSecond() + now.getNano() / 1e9,
        Double.parseDouble(traits.asFloatForm().toString()),
        0.0000000001);
  }

  @Test
  public void testZonedDateTime() {
    Instant now = Instant.now();
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);
    TemporalAccessorTraits traits = new TemporalAccessorTraits(zonedDateTime);

    assertEquals(zonedDateTime, traits.asTemporalAccessor());
    assertEquals(zonedDateTime.toString(), traits.asString());

    assertEquals(now.getEpochSecond(), traits.asInt());
    assertEquals(Long.toHexString(now.toEpochMilli()), traits.asIntForm().toHexString());

    assertEquals(
        now.getEpochSecond() + now.getNano() / 1e9,
        Double.parseDouble(traits.asFloatForm().toString()),
        0.0000000001);
  }
}
