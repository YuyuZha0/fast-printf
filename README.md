# fast-printf

[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuyuzha0/fast-printf.svg?style=flat-square)](https://search.maven.org/artifact/io.github.yuyuzha0/fast-printf)
[![License](https://img.shields.io/badge/License-GPL--2.0--with--classpath--exception-blue.svg?style=flat-square)](https://openjdk.java.net/legal/gplv2+ce.html)

A high-performance, `glibc`-compliant, and garbage-conscious `printf`-style formatter for Java 8+.

Tired of `String.format()` showing up in your profiler? `fast-printf` is a formatting library designed for
performance-critical applications where standard utilities become a bottleneck. It achieves significant speedups through
a **compile-once, run-many** approach and a sophisticated zero-copy architecture that minimizes memory allocations.

## Key Features

* 🚀 **High Performance**: Up to **4x faster** than `String.format()` by pre-compiling the format string into a reusable,
  thread-safe formatter.
* 🗑️ **Low to Zero Allocation**: Employs a rope-like character sequence data structure (`Seq`) for internal string
  building. This avoids creating intermediate strings and character arrays, dramatically reducing GC pressure in hot
  loops.
* ⚙️ **Glibc Compatible**: Adheres to the widely-used `glibc` `printf` conventions (from C/C++), making it familiar and
  predictable, rather than following the `java.util.Formatter` specification.
* 💡 **Fluent, No-Boxing API**: Provides a fluent builder (`Args.create().putInt(...)`) that allows passing primitive
  arguments without any boxing overhead, maximizing performance in critical code paths.
* 🧩 **Zero Dependencies**: A lightweight library with no external dependencies.
* ☕ **Java 8+**: Compatible with all modern Java runtimes.

## Performance

`fast-printf` is designed for speed. The pre-compilation and low-allocation strategy pays off significantly in tight
loops.

| Benchmark (`avgt`, ns/op)               | Score    | Notes                                                                |
|-----------------------------------------|----------|----------------------------------------------------------------------|
| **`fastPrintf` (using `Args` builder)** | **~907** | The core library performance with no boxing.                         |
| `fastPrintf` (with `ThreadLocal` cache) | ~1010    | Opt-in cache; can help for very large strings but has its own costs. |
| `jdkPrintf` (`String.format`)           | ~3767    | The baseline for comparison.                                         |

*Lower scores are better. Benchmarks run with JMH. Source code is available in the `benchmark` package.*

## When to use `fast-printf`

This library is ideal for performance-sensitive applications:

* **High-throughput logging**: Formatting log messages in tight, performance-critical loops.
* **Data Serialization**: Generating text-based data formats (e.g., CSV, protocol messages) at high speed.
* **Real-time systems**: Financial applications, game engines, or monitoring agents where GC pauses must be minimized.
* Anywhere `String.format()` has been identified as a performance bottleneck.

For general-purpose string formatting where performance is not the primary concern, the standard `String.format()` is
often sufficient.

## Installation

### Maven

```xml

<dependency>
    <groupId>io.github.yuyuzha0</groupId>
    <artifactId>fast-printf</artifactId>
    <version>1.1.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.yuyuzha0:fast-printf:1.1.1'
```

## Usage

The core idea is to compile a format string once into a `FastPrintf` instance and reuse it.

```java
import io.fastprintf.Args;
import io.fastprintf.FastPrintf;

import java.time.LocalDateTime;

public class Example {
    // Compile once and reuse. The FastPrintf instance is immutable and thread-safe.
    private static final FastPrintf FORMATTER = FastPrintf.compile(
            "ID: %#08X, Score: %05.2f, User: %.5S, Time: %t{yyyy-MM-dd HH:mm:ss}"
    );

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Using varargs - Simple and convenient
        String result1 = FORMATTER.format(255, Math.PI, "test-user", now);
        System.out.println(result1);
        // Output: ID: 0X0000FF, Score: 03.14, User: TEST-, Time: 2023-10-27 10:30:00

        // 2. Using the fluent Args builder - Maximum performance, no boxing
        Args args = Args.create()
                .putInt(255)
                .putDouble(Math.PI)
                .putString("test-user")
                .putDateTime(now);
        String result2 = FORMATTER.format(args);
        System.out.println(result2);
        // Output: ID: 0X0000FF, Score: 03.14, User: TEST-, Time: 2023-10-27 10:30:00
    }
}
```

### Choosing an `Args` Style

`fast-printf` offers two ways to provide arguments, each with a specific purpose:

* **Convenience:** `FORMATTER.format(123, "test")` or `Args.of(123, "test")`.
    * This is the easiest and most readable method.
    * It uses varargs (`Object...`), which involves **auto-boxing** primitive types (e.g., `int` becomes `Integer`).
      This is fine for most use cases.

* **Performance:** `Args.create().putInt(123).putString("test")`.
    * This fluent builder API is designed for performance-critical code.
    * Methods like `putInt(int)` and `putDouble(double)` accept unboxed primitives, **avoiding all allocation and boxing
      overhead** for those arguments. Use this style inside hot loops.

## How It Works: Under the Hood

The performance of `fast-printf` comes from three key architectural pillars:

1. **Ahead-of-Time Compiler**: `FastPrintf.compile()` parses the format string into a series of `Appender` objects—a
   list of optimized formatting steps. This work is done only once.
2. **Zero-Copy String Building**: The library uses an internal, rope-like `Seq` data structure. When concatenating
   formatted parts, it creates lightweight wrapper objects instead of copying characters. The final `String` is rendered
   in a single, efficient pass at the very end.
3. **Ahead-of-Time Argument Processing**: The `Args` object converts your arguments into a list of `FormatTraits`
   —specialized, type-aware handlers. This eliminates `instanceof` checks and reflection from the critical formatting
   loop.

## API Reference

The format string syntax is:
`%[flags][width][.precision]specifier[{date-time-pattern}]`

---

### Custom Date/Time Formatting

A powerful extension is the ability to provide an inline `DateTimeFormatter` pattern for the `%t` and `%T` specifiers.

* **Syntax**: `%t{pattern}`
* **Example**: `%t{yyyy-MM-dd'T'HH:mm:ss.SSSZ}`
* **Default**: If no pattern is provided (`%t`), it defaults to `DateTimeFormatter.ISO_OFFSET_DATE_TIME`.

---

### Specifiers

| Specifier  | Output                                                                                  | Example                      |
|:----------:|-----------------------------------------------------------------------------------------|------------------------------|
| `d` or `i` | Signed decimal integer                                                                  | `392`                        |
|    `u`     | Unsigned decimal integer                                                                | `7235`                       |
|    `o`     | Unsigned octal                                                                          | `610`                        |
|    `x`     | Unsigned hexadecimal integer (lowercase)                                                | `7fa`                        |
|    `X`     | Unsigned hexadecimal integer (uppercase)                                                | `7FA`                        |
| `f` / `F`  | Decimal floating point                                                                  | `392.65`                     |
|    `e`     | Scientific notation (lowercase `e`)                                                     | `3.9265e+2`                  |
|    `E`     | Scientific notation (uppercase `E`)                                                     | `3.9265E+2`                  |
| `g` / `G`  | Shortest representation of `%e` or `%f`                                                 | `392.65`                     |
| `a` / `A`  | Hexadecimal floating point (lowercase/uppercase `p`)                                    | `-0xc.90fep-2`               |
|    `c`     | Character                                                                               | `a`                          |
|    `s`     | String of characters (from `Object.toString()`)                                         | `sample`                     |
|    `S`     | String of characters, **converted to uppercase**                                        | `SAMPLE`                     |
| `t` / `T`  | Date/Time string (case affects final string output)                                     | `2023-12-31T23:59:59+01:00`  |
|    `p`     | Object "pointer" (class name + identity hash). Throws an exception for primitive types. | `java.lang.Integer@707f7052` |
|    `n`     | Nothing printed. The argument is consumed.                                              |                              |
|    `%`     | A literal `%` character                                                                 | `%`                          |

---

### Flags

|    Flag     | Description                                                                                                                                                                                                                     |
|:-----------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|     `-`     | Left-aligns the result within the field width.                                                                                                                                                                                  |
|     `+`     | Forces the result to be prefixed with a sign (`+` or `-`), even for positive numbers. Overrides the space flag.                                                                                                                 |
| ` ` (space) | Prefixes positive numbers with a space. Ignored if the `+` flag is present.                                                                                                                                                     |
|     `#`     | Alternate form: <ul><li>For `o`, prefixes with `0`.</li><li>For `x`/`X`, prefixes with `0x`/`0X`.</li><li>For `f`, `e`, `g`, etc., forces a decimal point.</li><li>For `g`/`G`, prevents stripping of trailing zeros.</li></ul> |
|     `0`     | Pads the output with leading zeros (instead of spaces) to meet the specified width. Ignored if `-` is present or if precision is specified for an integer.                                                                      |

---

### Width and Precision

| Field        | Description                                                                                                                                                                                                                                                                                                 |
|:-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `width`      | Minimum characters to print. Padded with spaces (or zeros with `0` flag). Never truncates. `*` reads width from the next `int` argument.                                                                                                                                                                    |
| `.precision` | <ul><li>**Integers:** Minimum number of digits (zero-padded).</li><li>**Floats (`f`, `e`):** Digits after the decimal point.</li><li>**Floats (`g`):** Max significant digits.</li><li>**String (`s`, `S`):** Max characters to print.</li><li>`.*` reads precision from the next `int` argument.</li></ul> |

## Comparison with `String.format()`

`fast-printf` intentionally differs from Java's `String.format` to align with `glibc` and maximize performance:

* **Glibc vs. Java `Formatter` Conventions**: Follows `glibc` `printf`. For example, `%S` converts the entire string to
  uppercase, unlike Java's wide character formatting.
* **No Argument Indexing**: Features like `%2$s` are not supported. Arguments are always consumed sequentially.
* **No Locale Support**: Formatting is locale-agnostic for performance (`.` is always the decimal separator).
* **Type-Safe `%p`**: The `%p` specifier works only for object references and will correctly throw an exception if given
  a primitive type, preventing bugs related to auto-boxing.

## License

`fast-printf` is licensed under the **GNU General Public License v2 with Classpath Exception**, the same license as the
OpenJDK.

This library includes derivative works from OpenJDK (specifically for high-fidelity floating-point formatting). These
files retain their original headers and are governed by the terms of the GPLv2+CE.