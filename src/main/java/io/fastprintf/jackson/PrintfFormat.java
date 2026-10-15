package io.fastprintf.jackson;

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
 * registration to take effect. Without the module, Jackson ignores this annotation. With the
 * module, it selects {@link FastPrintfSerializer}; explicitly selecting a different serializer for
 * the same property is a configuration error.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrintfFormat {
  /** Returns the format pattern, compiled when creating the property serializer. */
  String value();
}
