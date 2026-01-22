# fast-printf

[![Java CI](https://github.com/YuyuZha0/fast-printf/actions/workflows/maven.yml/badge.svg)](https://github.com/YuyuZha0/fast-printf/actions/workflows/maven.yml)
[![codecov](https://codecov.io/github/YuyuZha0/fast-printf/graph/badge.svg?token=UPPTCS4DRS)](https://codecov.io/github/YuyuZha0/fast-printf)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuyuzha0/fast-printf.svg?style=flat-square)](https://search.maven.org/artifact/io.github.yuyuzha0/fast-printf)
[![License](https://img.shields.io/badge/License-GPL--2.0--with--classpath--exception-blue.svg?style=flat-square)](https://openjdk.java.net/legal/gplv2+ce.html)

A high-performance, `glibc`-compliant, and low-allocation `printf`-style formatter for Java 8+.

`fast-printf` is a specialized formatting library designed for performance-critical applications where standard
utilities like `String.format()` become a bottleneck. It achieves significant speedups through a **compile-once,
run-many** approach and a sophisticated architecture that minimizes memory allocations and garbage collection pressure.

## Key Features

* 🚀 **High Performance**: Consistently outperforms `String.format()` across all Java versions. The advantage is most
  significant on older runtimes (up to **4x faster** on JDK 8), and remains substantial even on modern runtimes (~**3x
  faster** on JDK 21).
* 🗑️ **Low Allocation**: Employs a rope-like character sequence data structure for internal string building. This avoids
  creating intermediate strings and character arrays, dramatically reducing GC pressure in hot loops.
* ⚙️ **Glibc Compatible**: Adheres to the widely-used `glibc` `printf` conventions (from C/C++), providing familiar and
  predictable behavior rather than following the `java.util.Formatter` specification.
* 💡 **State-of-the-Art Float Formatting**: **Backports the high-fidelity floating-point formatting engine from OpenJDK
  21.** This brings the modern "Schubfach" algorithm to Java 8+, ensuring correctly rounded and the shortest possible
  output for `double` and `float` values—a level of accuracy not available in older JDKs' `String.format()`.
* ⛓️ **Fluent, No-Boxing API**: Provides a fluent builder (`Args.create().putInt(...)`) that accepts primitive arguments
  without any boxing overhead, maximizing performance in critical code paths.
* 🧩 **Zero Dependencies**: A lightweight library with no external dependencies.
* ☕ **Java 8+**: Compatible with all modern Java runtimes.

## Performance (JDK 21)

While `fast-printf` provides a significant speedup on all platforms, it's important to understand how the landscape is
changing. The following benchmarks were run on **JDK 21**, where `String.format()` has received substantial
optimizations.

The benchmark uses a complex format string (`%#018x|%-15.7g|%S|%c|%d|%15.5f`) to stress the formatting logic over simple
string concatenation.

| Benchmark (`avgt`, ns/op)               | Score    | Notes                                                     |
|-----------------------------------------|----------|-----------------------------------------------------------|
| **`fastPrintf` (varargs)**              | **~394** | The core library performance with auto-boxing.            |
| `fastPrintf` (with `ThreadLocal` cache) | ~466     | Opt-in cache; can have higher overhead for short strings. |
| `jdkPrintf` (`String.format`)           | ~1218    | The baseline for comparison on a modern JDK.              |

*Lower scores are better. Source: `ComplexFormatBenchmark`.*

### Performance on Older JDKs

The performance advantage of `fast-printf` is even more pronounced on older runtimes like **Java 8 or 11**, where
`String.format()` is less optimized. On these versions, speedups of **up to 4x** are common for complex formats.

Across all versions, the primary advantage of `fast-printf` remains its **dramatically lower memory allocation**, which
leads to reduced GC pressure in high-throughput applications.

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
    <version>1.2.8</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.yuyuzha0:fast-printf:1.2.8'
```

## Usage

The core idea is to compile a format string once into a `FastPrintf` instance and reuse it for all subsequent formatting
operations.

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

### Convenience vs. Maximum Performance

`fast-printf` offers two ways to provide arguments, each with a specific purpose:

* **Convenience:** `FORMATTER.format(123, "test")` or `Args.of(123, "test")`.
    * This is the easiest and most readable method.
    * It uses varargs (`Object...`), which involves **auto-boxing** primitive types (e.g., `int` becomes `Integer`).
      This is fine for most use cases.

* **Maximum Performance:** `Args.create().putInt(123).putString("test")`.
    * This fluent builder API is designed for performance-critical code.
    * Methods like `putInt(int)` and `putDouble(double)` accept unboxed primitives, **avoiding all allocation and boxing
      overhead** for those arguments. Use this style inside hot loops.

## Advanced: JDK 21 Floating-Point Formatting on Java 8

One of the key features of `fast-printf` is its superior floating-point formatting, which provides correctness and
performance guarantees unavailable in standard Java 8.

### The Problem

The `String.format()` implementation in older JDKs (prior to JDK 18) had known issues with floating-point-to-decimal
conversion. It could produce results that were not the shortest, correctly-rounded representation, leading to subtle
accuracy bugs, especially in scientific and financial applications.

### The Solution

`fast-printf` directly **backports the modern floating-point formatting engine from OpenJDK 21**. This engine is based
on the advanced "Schubfach" algorithm, which guarantees the shortest, most accurate decimal representation of a binary
floating-point number.

By including this modern implementation, `fast-printf` ensures that your floating-point numbers are formatted with
state-of-the-art precision and correctness, even when your application is running on Java 8.

## How It Works: Under the Hood

The performance of `fast-printf` comes from four key architectural pillars:

1. **Ahead-of-Time Compiler**: `FastPrintf.compile()` parses the format string into a series of `Appender` objects—a
   list of optimized formatting steps. This work is done only once.
2. **Zero-Copy String Building**: The library uses an internal, rope-like `Seq` data structure. When concatenating
   formatted parts, it creates lightweight wrapper objects instead of copying characters. The final `String` is rendered
   in a single, efficient pass at the very end.
3. **Ahead-of-Time Argument Processing**: The `Args` object converts your arguments into a list of `FormatTraits`
   —specialized, type-aware handlers. This eliminates `instanceof` checks and reflection from the critical formatting
   loop.
4. **Backported Formatting Logic**: As mentioned above, it incorporates the modern, high-performance `DoubleToDecimal`
   logic from OpenJDK 21 to ensure float/double formatting is both fast and mathematically correct on all supported Java
   versions.

## API Reference

The format string syntax is:
`%[flags][width][.precision]specifier[{date-time-pattern}]`

---

### Custom Date/Time Formatting

A powerful extension is the ability to provide an inline `DateTimeFormatter` pattern for the `%t` and `%T` specifiers.

* **Syntax**: `%t{pattern}`
* **Example**: `%t{yyyy-MM-dd'T'HH:mm:ss.SSSZ}`
* **Default**: If no pattern is provided (`%t`), an appropriate ISO formatter is chosen based on the argument type (
  e.g., `ISO_OFFSET_DATE_TIME` for a `ZonedDateTime`).

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

|    Flag     | Description                                                                                                                                                                                                               |
|:-----------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|     `-`     | Left-aligns the result within the field width.                                                                                                                                                                            |
|     `+`     | Forces the result to be prefixed with a sign (`+` or `-`), even for positive numbers. Overrides the space flag.                                                                                                           |
| ` ` (space) | Prefixes positive numbers with a space. Ignored if the `+` flag is present.                                                                                                                                               |
|     `#`     | Alternate form: <ul><li>For `o`, prefixes with `0`.</li><li>For `x`/`X`, prefixes with `0x`/`0X`.</li><li>For `f`, `e`, `g`, forces a decimal point.</li><li>For `g`/`G`, prevents stripping of trailing zeros.</li></ul> |
|     `0`     | Pads the output with leading zeros (instead of spaces) to meet the specified width. Ignored if `-` is present or if precision is specified for an integer.                                                                |

---

### Width and Precision

| Field        | Description                                                                                                                                                                                                                                                                                                 |
|:-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `width`      | Minimum characters to print. Padded with spaces (or zeros with `0` flag). Never truncates. `*` reads width from the next `int` argument.                                                                                                                                                                    |
| `.precision` | <ul><li>**Integers:** Minimum number of digits (zero-padded).</li><li>**Floats (`f`, `e`):** Digits after the decimal point.</li><li>**Floats (`g`):** Max significant digits.</li><li>**String (`s`, `S`):** Max characters to print.</li><li>`.*` reads precision from the next `int` argument.</li></ul> |

## Key Differences from `String.format()`

`fast-printf` intentionally differs from Java's `String.format` to align with `glibc` conventions and maximize
performance:

* **Glibc vs. Java `Formatter` Conventions**: Follows `glibc` `printf`. For example, `%S` converts the entire string to
  uppercase, unlike Java's behavior which is tied to `Formattable`.
* **`%p` (Pointer) Specifier**: Provides the C-style `%p` specifier to print an object's identity. This useful specifier
  is **not available** in Java's `String.format()`. The implementation is also type-safe and will correctly throw an
  exception if given a primitive type, preventing bugs related to auto-boxing.
* **No Argument Indexing**: Features like `%2$s` are not supported. Arguments are always consumed sequentially for
  maximum performance.
* **No Locale Support**: Formatting is locale-agnostic for performance (`.` is always the decimal separator).

## License

`fast-printf` is licensed under the **GNU General Public License v2 with Classpath Exception**, the same license used by
the OpenJDK.

This choice of license is deliberate, as this library includes internal utility classes that are derivative works of
OpenJDK (specifically for high-fidelity floating-point formatting). These backported files retain their original
copyright headers and are governed by the terms of the GPLv2+CE, and thus the library as a whole adopts this license.