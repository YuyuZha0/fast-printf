# fast-printf

[![Build Status](https://img.shields.io/travis/com/yuyuzha0/fast-printf.svg?style=flat-square)](https://travis-ci.com/yuyuzha0/fast-printf)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuyuzha0/fast-printf.svg?style=flat-square)](https://search.maven.org/artifact/io.github.yuyuzha0/fast-printf)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)

A high-performance, garbage-conscious `printf`-style formatter for Java 8+.

Tired of `String.format()` showing up in your profiler? `fast-printf` is a drop-in-friendly formatting library designed for performance-critical applications where standard formatting utilities become a bottleneck. It achieves significant speedups by compiling format strings once and minimizing memory allocations during formatting.

## Key Features

*   🚀 **High Performance**: Up to **4x faster** than `String.format()` in benchmarks by pre-compiling the format string and optimizing the formatting path.
*   🗑️ **Low to Zero Allocation**: Utilizes advanced techniques like rope-like character sequences to avoid creating intermediate strings, reducing GC pressure in hot loops.
*   ⚙️ **Glibc Compatible**: Follows the widely-used `glibc` `printf` conventions, making it familiar to C/C++/Python/Ruby developers, rather than the `java.util.Formatter` conventions.
*   🧩 **Zero Dependencies**: A lightweight library with no external dependencies.
*   ☕ **Java 8+**: Compatible with modern Java runtimes.

## Performance

`fast-printf` is designed for speed. The pre-compilation and low-allocation strategy pays off significantly in tight loops.

| Benchmark (`avgt`, ns/op)                    | Score      | Notes                                    |
| -------------------------------------------- | ---------- | ---------------------------------------- |
| **`fastPrintf`**                             | **~907**   | The core library performance.            |
| `fastPrintf` (with `ThreadLocal` cache)      | ~1010      | Opt-in cache, useful for large strings.  |
| `jdkPrintf` (`String.format`)                | ~3767      | The baseline for comparison.             |

*Lower scores are better. Benchmarks run with JMH. Source code is available in the [`benchmark` package](test/java/io/fastprintf/benchmark/).*

## When to use `fast-printf`

This library is ideal for performance-sensitive applications:
*   **High-throughput logging**: Formatting log messages in a tight loop.
*   **Data Serialization**: Generating text-based data formats (e.g., CSV, JSON) at high speed.
*   **Real-time systems**: Financial applications, game engines, or monitoring agents where GC pauses must be minimized.
*   Anywhere `String.format()` has been identified as a performance bottleneck.

For general-purpose string formatting where performance is not critical, the standard `String.format()` is often sufficient.

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
    // Compile once and reuse. The FastPrintf instance is thread-safe.
    private static final FastPrintf FORMATTER = FastPrintf.compile(
        "%#08X, %05.2f, %.5S, %{yyyy-MM-dd HH:mm:ss}t"
    );

    public static void main(String[] args) {
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59);

        // 1. Using varargs (simple and convenient)
        String result1 = FORMATTER.format(123456789L, Math.PI, "Hello World", dateTime);
        System.out.println(result1);
        // Output: 0X75BCD15, 03.14, HELLO, 2023-12-31 23:59:59

        // 2. Using an Args object (useful for collections or dynamic arguments)
        Args args = Args.of(123456789L, Math.PI, "Hello World", dateTime);
        String result2 = FORMATTER.format(args);
        System.out.println(result2);
        // Output: 0X75BCD15, 03.14, HELLO, 2023-12-31 23:59:59

        // 3. Using the fluent Args builder (type-safe and clear)
        Args argsBuilder = Args.create()
                .putLong(123456789L)
                .putDouble(Math.PI)
                .putString("Hello World")
                .putDateTime(dateTime);
        String result3 = FORMATTER.format(argsBuilder);
        System.out.println(result3);
        // Output: 0X75BCD15, 03.14, HELLO, 2023-12-31 23:59:59
    }
}
```

## API Reference

### Format Syntax

The format string syntax is:
`%[flags][width][.precision][{date-time-pattern}]specifier`

---

### Custom Date/Time Formatting

A powerful extension is the ability to provide an inline `DateTimeFormatter` pattern for the `%t` and `%T` specifiers.

*   **Syntax**: `%{pattern}t`
*   **Example**: `%{yyyy-MM-dd'T'HH:mm:ss.SSSZ}t`
*   **Default**: If no pattern is provided (`%t`), it defaults to `DateTimeFormatter.ISO_OFFSET_DATE_TIME`.

---

### Specifiers

| Specifier | Output                                        | Example                    |
|:---------:|-----------------------------------------------|----------------------------|
| `d` or `i`| Signed decimal integer                        | `392`                      |
| `u`       | Unsigned decimal integer                      | `7235`                     |
| `o`       | Unsigned octal                                | `610`                      |
| `x`       | Unsigned hexadecimal integer (lowercase)      | `7fa`                      |
| `X`       | Unsigned hexadecimal integer (uppercase)      | `7FA`                      |
| `f` / `F` | Decimal floating point                        | `392.65`                   |
| `e`       | Scientific notation (lowercase `e`)           | `3.9265e+2`                |
| `E`       | Scientific notation (uppercase `E`)           | `3.9265E+2`                |
| `g` / `G` | Shortest representation of `%e` or `%f`         | `392.65`                   |
| `a` / `A` | Hexadecimal floating point (lowercase/uppercase `p`)| `-0xc.90fep-2`       |
| `c`       | Character                                     | `a`                        |
| `s`       | String of characters                          | `sample`                   |
| `S`       | String of characters, **converted to uppercase** | `SAMPLE`                  |
| `t` / `T` | Date/Time string (case affects final string)  | `2023-12-31T23:59:59+01:00`|
| `p`       | Java object "pointer" address (like `Object.toString()`) | `java.lang.Integer@707f7052` |
| `n`       | Nothing printed. The argument is consumed.    |                            |
| `%`       | A literal `%` character                       | `%`                        |

---

### Flags

| Flag      | Description                                                                                                                                                                                                             |
|:---------:|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-`       | Left-aligns the result within the field width.                                                                                                                                                                          |
| `+`       | Forces the result to be prefixed with a sign (`+` or `-`), even for positive numbers. Overrides the space flag.                                                                                                            |
| ` ` (space) | Prefixes positive numbers with a space. Ignored if the `+` flag is present.                                                                                                                                             |
| `#`       | Alternate form: <ul><li>For `o`, prefixes with `0`.</li><li>For `x`/`X`, prefixes with `0x`/`0X`.</li><li>For `f`, `e`, `g`, etc., forces a decimal point even if not needed.</li><li>For `g`/`G`, prevents stripping of trailing zeros.</li></ul> |
| `0`       | Pads the output with leading zeros (instead of spaces) to meet the specified width. Ignored if the `-` flag is present or if precision is specified for an integer.                                                        |

---

### Width

| Width    | Description                                                                                                                                                               |
|:---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `(number)` | Minimum number of characters to print. If the value is shorter, it is padded with spaces (or zeros if `0` flag is used). The value is never truncated.                     |
| `*`        | The width is read from the next argument in the list (which must be an integer). For example, `format("%*d", 5, 10)` is equivalent to `format("%5d", 10)`. |

---

### Precision

| Precision | Description                                                                                                                                                                                                                                                                |
|:----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `.(number)` | <ul><li>**Integers (`d`, `i`, `o`, `u`, `x`):** Minimum number of digits to display, padded with leading zeros if necessary. A value of `0` with precision `0` produces no output.</li><li>**Floating-Point (`f`, `e`):** Number of digits after the decimal point.</li><li>**Floating-Point (`g`):** Maximum number of significant digits.</li><li>**String (`s`):** Maximum number of characters to print from the string.</li></ul> |
| `.*`      | The precision is read from the next argument in the list (which must be an integer). For example, `format("%.*f", 3, 3.14159)` is equivalent to `format("%.3f", 3.14159)`. |

## Comparison with `String.format()`

`fast-printf` intentionally differs from Java's `String.format` to align with `glibc` and maximize performance:
*   **Convention**: Follows `glibc` `printf` where possible. For example, `%S` converts to uppercase, not for wide characters.
*   **No Argument Indexing**: Features like `%2$s` are not supported. Arguments are always consumed sequentially.
*   **No Locale Support**: Formatting is locale-agnostic for performance reasons (e.g., `.` is always the decimal separator).

## License

`fast-printf` is licensed under the [GNU GENERAL PUBLIC LICENSE
Version 2](LICENSE).

This library includes derivative works from OpenJDK, which is licensed under GPLv2 with the Classpath Exception. These files retain their original headers and are used to provide accurate and high-performance floating-point formatting.
```