package io.fastprintf.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Formats a Jackson property as a JSON string using a {@code FastPrintf} pattern.
 *
 * <p>For example, {@code @PrintfFormat("%.2f")} writes a numeric value as {@code "12.30"}. The
 * property supplies one formatting argument. Jackson handles JSON quoting, escaping and Java null
 * values. This annotation affects serialization only and requires {@link FastPrintfModule}
 * registration. This annotation selects {@link FastPrintfSerializer}; explicitly selecting a
 * different serializer for the same property is a configuration error.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = FastPrintfSerializer.class)
public @interface PrintfFormat {
  /** Returns the format pattern, compiled when creating the property serializer. */
  String value();

  /**
   * Enables the formatter's per-thread reusable builder. Defaults to {@code false}. Enabling this
   * option bypasses the module's shared pattern cache.
   */
  boolean enableThreadLocalCache() default false;

  /**
   * Sets the initial builder capacity to a positive number, or {@code -1} to use the formatter's
   * default. Specifying a capacity bypasses the module's shared pattern cache.
   */
  int stringBuilderInitialCapacity() default -1;
}
