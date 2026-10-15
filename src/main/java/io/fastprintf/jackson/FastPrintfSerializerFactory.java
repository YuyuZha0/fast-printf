package io.fastprintf.jackson;

import io.fastprintf.FastPrintf;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Creates fully configured serializers and owns the module's shared pattern cache. */
final class FastPrintfSerializerFactory {
  private final ConcurrentMap<String, FastPrintf> formatters = new ConcurrentHashMap<>();

  FastPrintfSerializer create(PrintfFormat annotation) {
    FastPrintf formatter = formatters.computeIfAbsent(annotation.value(), FastPrintf::compile);
    return new FastPrintfSerializer(formatter);
  }
}
