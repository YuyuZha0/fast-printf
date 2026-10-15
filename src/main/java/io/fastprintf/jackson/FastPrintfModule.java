package io.fastprintf.jackson;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Shares compiled {@link PrintfFormat} patterns across properties using this module instance.
 *
 * <p>Register with {@code mapper.registerModule(new FastPrintfModule())} before using the mapper.
 * The cache retains one formatter per distinct annotation pattern for the lifetime of the module
 * and its introspector. It is thread-safe and is only consulted during serializer creation, not on
 * each serialization. Sharing a module instance between mappers also shares its cache.
 *
 * <p>The module supplies the serializer instances required by {@link PrintfFormat}.
 */
public final class FastPrintfModule extends SimpleModule {
  private final FastPrintfSerializerFactory factory = new FastPrintfSerializerFactory();

  /** Creates a module with its own initially empty pattern cache. */
  public FastPrintfModule() {
    super("FastPrintfModule");
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);
    context.insertAnnotationIntrospector(new PrintfIntrospector(factory));
  }

  private static final class PrintfIntrospector extends NopAnnotationIntrospector {
    private final FastPrintfSerializerFactory factory;

    private PrintfIntrospector(FastPrintfSerializerFactory factory) {
      this.factory = factory;
    }

    @Override
    public Object findSerializer(Annotated annotated) {
      JsonSerialize annotation = annotated.getAnnotation(JsonSerialize.class);
      PrintfFormat format = annotated.getAnnotation(PrintfFormat.class);
      if (annotation != null && annotation.using() != JsonSerializer.None.class) {
        if (annotation.using() != FastPrintfSerializer.class) {
          if (format != null) {
            throw new IllegalArgumentException(
                "@PrintfFormat conflicts with @JsonSerialize(using = "
                    + annotation.using().getName()
                    + "): expected FastPrintfSerializer");
          }
          return null;
        }
        if (format == null) {
          throw new IllegalArgumentException(
              "FastPrintfSerializer requires @PrintfFormat with an explicit pattern");
        }
      }
      return format == null ? null : factory.create(format);
    }
  }
}
