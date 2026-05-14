# fast-printf

[![Java CI](https://github.com/YuyuZha0/fast-printf/actions/workflows/maven.yml/badge.svg)](https://github.com/YuyuZha0/fast-printf/actions/workflows/maven.yml)
[![codecov](https://codecov.io/github/YuyuZha0/fast-printf/graph/badge.svg?token=UPPTCS4DRS)](https://codecov.io/github/YuyuZha0/fast-printf)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuyuzha0/fast-printf.svg?style=flat-square)](https://search.maven.org/artifact/io.github.yuyuzha0/fast-printf)
[![License](https://img.shields.io/badge/License-GPL--2.0--with--classpath--exception-blue.svg?style=flat-square)](https://openjdk.java.net/legal/gplv2+ce.html)

A high-performance, `glibc`-compliant, and low-allocation `printf`-style formatter for Java 8+.

`fast-printf` is a specialized formatting library designed for performance-critical applications where standard
utilities like `String.format()` become a bottleneck. It achieves significant speedups through a **compile-once,
run-many** approach and a sophisticated architecture that minimizes memory allocations and garbage collection pressure.

## Contents

- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [When to use fast-printf](#when-to-use-fast-printf)
- [Performance (JDK 21)](#performance-jdk-21)
- [Installation](#installation)
- [Usage](#usage)
- [Advanced: JDK 21 Floating-Point on Java 8](#advanced-jdk-21-floating-point-on-java-8)
- [How It Works](#how-it-works)
- [API Reference](#api-reference)
- [Key Differences from `String.format()`](#key-differences-from-stringformat)
- [Contributing](#contributing)
- [License](#license)

## Quick Start

Add the dependency (Maven):

```xml
<dependency>
    <groupId>io.github.yuyuzha0</groupId>
    <artifactId>fast-printf</artifactId>
    <version>1.2.13</version>
</dependency>
```

Compile once, format many times:

```java
import io.fastprintf.FastPrintf;

private static final FastPrintf F = FastPrintf.compile("User %s (id=%d) scored %.2f");

String s = F.format("Alice", 42, 99.5);
// → "User Alice (id=42) scored 99.50"
```

That's the whole API for typical use. The rest of this document covers the no-boxing `Args` builder, the format-string
syntax, and the design trade-offs.

## Key Features

* 🚀 **High Performance**: Consistently outperforms `String.format()` across all Java versions. The advantage is most
  significant on older runtimes (up to **4x faster** on JDK 8), and remains substantial even on modern runtimes (~**2x
  faster** on JDK 21 for typical log-line formats).
* 🗑️ **Low Allocation**: Employs a rope-like character sequence data structure for internal string building. This avoids
  creating intermediate strings and character arrays, dramatically reducing GC pressure in hot loops.
* ⚙️ **Glibc Compatible**: Adheres to the widely-used `glibc` `printf` conventions (from C/C++), providing familiar and
  predictable behavior rather than following the `java.util.Formatter` specification.
* 💡 **State-of-the-Art Float Formatting**: Backports the high-fidelity floating-point formatting engine from
  **OpenJDK 21** (the "Schubfach" algorithm), so `double` and `float` output is correctly rounded and shortest-possible
  even on Java 8.
  See [Advanced: JDK 21 Floating-Point on Java 8](#advanced-jdk-21-floating-point-on-java-8).
* ⛓️ **Fluent, No-Boxing API**: Provides a fluent builder (`Args.create().putInt(...)`) that accepts primitive arguments
  without any boxing overhead, maximizing performance in critical code paths.
* 🧩 **Zero Dependencies**: A lightweight library with no external dependencies.
* ☕ **Java 8+**: Compatible with all modern Java runtimes.

## When to use fast-printf

Ideal for:

* **High-throughput logging**: Formatting log messages in tight, performance-critical loops.
* **Data serialization**: Generating text-based data formats (CSV, protocol messages) at high speed.
* **Real-time systems**: Financial applications, game engines, or monitoring agents where GC pauses must be minimized.
* Anywhere `String.format()` has been identified as a hot path.

For general-purpose string formatting where performance is not the primary concern, the standard `String.format()` is
often sufficient.

## Performance (JDK 21)

Benchmarked on **JDK 21** (Corretto 21.0.9), where `String.format()` has received substantial optimizations. The
format string `[%s] %s id=%d latency=%.3fms` represents a realistic log-line use case covering the most common printf
patterns: literal text mixed with `%s`, `%d`, and `%.Nf`. The same string is fed to both `FastPrintf.compile(...)` and
`String.format(...)` — no per-side translation or workarounds.

| Benchmark (`avgt`, ns/op)                | Score    | Notes                                                              |
|------------------------------------------|----------|--------------------------------------------------------------------|
| **`fastPrintf` (varargs)**               | **~227** | The core library performance with auto-boxing.                     |
| `fastPrintf` (`Args` builder, no-boxing) | ~246     | Fluent primitive builder; trades a small constant for zero boxing. |
| `fastPrintf` (with `ThreadLocal` cache)  | ~226     | Opt-in cache; helps in tight reuse loops.                          |
| `jdkPrintf` (`String.format`)            | ~488     | The baseline for comparison on a modern JDK.                       |

*Lower scores are better. ~**2.15× faster** than `String.format()` on a typical log-line format.
Source: `CommonUsageBenchmark`.*

**Older JDKs.** On Java 8 / 11, where `String.format()` is less optimized, speedups of up to **4×** are common —
particularly for complex formats. Across all versions, the primary advantage of `fast-printf` is **dramatically lower
memory allocation**, which reduces GC pressure in high-throughput applications.

## Installation

**Maven:**

```xml
<dependency>
    <groupId>io.github.yuyuzha0</groupId>
    <artifactId>fast-printf</artifactId>
    <version>1.2.13</version>
</dependency>
```

**Gradle:**

```groovy
implementation 'io.github.yuyuzha0:fast-printf:1.2.13'
```

## Usage

The core idea is to compile a format string once into a `FastPrintf` instance and reuse it for all subsequent formatting
operations. Instances are immutable and thread-safe.

```java
import io.fastprintf.Args;
import io.fastprintf.FastPrintf;

public class Example {
    // Compile once and reuse. The FastPrintf instance is immutable and thread-safe.
    private static final FastPrintf FORMATTER =
            FastPrintf.compile("User %s (id=%d) scored %.2f");

    public static void main(String[] args) {
        // 1. Using varargs — simple and convenient
        String r1 = FORMATTER.format("Alice", 42, 99.5);
        System.out.println(r1);
        // → User Alice (id=42) scored 99.50

        // 2. Using the fluent Args builder — maximum performance, no boxing
        Args primitiveArgs = Args.create()
                .putString("Alice")
                .putInt(42)
                .putDouble(99.5);
        String r2 = FORMATTER.format(primitiveArgs);
        System.out.println(r2);
        // → User Alice (id=42) scored 99.50
    }
}
```

For richer formatting — uppercase strings (`%S`), zero-padding (`%05d`), hex (`%#08X`), date/time (`%t{...}`), etc. —
see the [API Reference](#api-reference) below.

### Convenience vs. maximum performance

| Style                                         | Boxing?           | When to use                                                |
|-----------------------------------------------|-------------------|------------------------------------------------------------|
| `FORMATTER.format(123, "test")` (varargs)     | Yes (primitives)  | Most call sites; readability wins.                         |
| `Args.of(123, "test")`                        | Yes (primitives)  | Same as varargs; useful when you build args incrementally. |
| `Args.create().putInt(123).putString("test")` | **No**            | Hot loops where every allocation matters.                  |

Both styles produce identical output.

## Advanced: JDK 21 Floating-Point on Java 8

`String.format()` on JDKs prior to 18 has known issues converting `double` / `float` to decimal: the output is not
always the shortest, correctly-rounded representation, leading to subtle accuracy bugs in scientific and financial
code. `fast-printf` backports the modern Schubfach-based engine from OpenJDK 21 so this correctness guarantee — and the
performance that comes with it — is available even when your application runs on Java 8.

## How It Works

The performance of `fast-printf` comes from four architectural pillars:

1. **Ahead-of-Time Compiler**: `FastPrintf.compile()` parses the format string once into a list of optimized `Appender`
   objects. Parsing never re-runs.
2. **Zero-Copy String Building**: An internal rope-like `Seq` data structure concatenates formatted parts with
   lightweight wrappers instead of copying characters. The final `String` is rendered in a single pass.
3. **Ahead-of-Time Argument Processing**: The `Args` object converts arguments into a list of `FormatTraits` —
   specialized, type-aware handlers. This eliminates `instanceof` checks and reflection from the formatting loop.
4. **Backported Float Logic**: Incorporates OpenJDK 21's `DoubleToDecimal` to ensure float/double formatting is both
   fast and mathematically correct on all supported Java versions.

## API Reference

Format string syntax:
`%[flags][width][.precision]specifier[{date-time-pattern}]`

### Custom date/time formatting

The `%t` / `%T` specifiers accept an inline `DateTimeFormatter` pattern.

* **Syntax**: `%t{pattern}`
* **Example**: `%t{yyyy-MM-dd'T'HH:mm:ss.SSSZ}`
* **Default**: If no pattern is provided (`%t`), an appropriate ISO formatter is chosen based on the argument type
  (e.g. `ISO_OFFSET_DATE_TIME` for a `ZonedDateTime`).

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

### Flags

|    Flag     | Description                                                                                                                                                                              |
|:-----------:|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|     `-`     | Left-aligns the result within the field width.                                                                                                                                           |
|     `+`     | Forces the result to be prefixed with a sign (`+` or `-`), even for positive numbers. Overrides the space flag.                                                                          |
| ` ` (space) | Prefixes positive numbers with a space. Ignored if the `+` flag is present.                                                                                                              |
|     `#`     | Alternate form:<br>• `o` → prefixes with `0`<br>• `x` / `X` → prefixes with `0x` / `0X`<br>• `f`, `e`, `g` → forces a decimal point<br>• `g` / `G` → prevents stripping of trailing zeros |
|     `0`     | Pads the output with leading zeros (instead of spaces) to meet the specified width. Ignored if `-` is present or if precision is specified for an integer.                               |

### Width and precision

| Field        | Description                                                                                                                                                                                                                                                              |
|:-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `width`      | Minimum characters to print. Padded with spaces (or zeros with `0` flag). Never truncates. `*` reads width from the next `int` argument.                                                                                                                                 |
| `.precision` | For each type:<br>• **Integers** — minimum number of digits (zero-padded)<br>• **Floats (`f`, `e`)** — digits after the decimal point<br>• **Floats (`g`)** — max significant digits<br>• **Strings (`s`, `S`)** — max characters to print<br>`.*` reads precision from the next `int` argument. |

## Key Differences from `String.format()`

`fast-printf` intentionally differs from Java's `String.format` to align with `glibc` conventions and maximize
performance:

* **Glibc vs. Java `Formatter` conventions**: Follows `glibc` `printf`. For example, `%S` converts the entire string to
  uppercase, unlike Java's behavior which is tied to `Formattable`.
* **`%p` (pointer) specifier**: Provides the C-style `%p` specifier to print an object's identity. This useful specifier
  is **not available** in Java's `String.format()`. The implementation is also type-safe and will correctly throw an
  exception if given a primitive type, preventing bugs related to auto-boxing.
* **No argument indexing**: Features like `%2$s` are not supported. Arguments are always consumed sequentially for
  maximum performance.
* **No locale support**: Formatting is locale-agnostic for performance (`.` is always the decimal separator).

## Contributing

Found a bug or have an idea? File it at the
[issue tracker](https://github.com/YuyuZha0/fast-printf/issues). Pull requests welcome.

## License

`fast-printf` is licensed under the **GNU General Public License v2 with Classpath Exception**, the same license used by
the OpenJDK.

This choice of license is deliberate, as this library includes internal utility classes that are derivative works of
OpenJDK (specifically for high-fidelity floating-point formatting). These backported files retain their original
copyright headers and are governed by the terms of the GPLv2+CE, and thus the library as a whole adopts this license.
