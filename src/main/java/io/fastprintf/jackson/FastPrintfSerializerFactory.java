package io.fastprintf.jackson;

import io.fastprintf.FastPrintf;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Creates fully configured serializers and owns the module's shared pattern cache. */
final class FastPrintfSerializerFactory {
  private final ConcurrentMap<String, FastPrintf> formatters = new ConcurrentHashMap<>();

  FastPrintfSerializer create(PrintfFormat annotation) {
    boolean threadLocal = annotation.enableThreadLocalCache();
    int capacity = annotation.stringBuilderInitialCapacity();
    FastPrintf formatter =
        threadLocal || capacity != -1
            ? FastPrintf.compile(annotation.value())
            : formatters.computeIfAbsent(annotation.value(), FastPrintf::compile);
    if (threadLocal) {
      formatter = formatter.enableThreadLocalCache();
    }
    if (capacity != -1) {
      formatter = formatter.setStringBuilderInitialCapacity(capacity);
    }
    return new FastPrintfSerializer(formatter);
  }
}
