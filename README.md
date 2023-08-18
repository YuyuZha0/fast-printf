## fast-printf

`fast-printf` is a Java library for fast printf-like formatting. Features:

* Can be extremely fast , about **4x** faster than `String.format`.
* Compatible with `glibc` printf convention, rather than Java `String.format` convention.
* Zero dependency. No external dependencies, requires only Java 8+.

## Usage

```xml

<dependency>
    <groupId>io.github.yuyuzha0</groupId>
    <artifactId>fast-printf</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
import io.fastprintf.Args;
import io.fastprintf.FastPrintf;

public class Usage {
    @Test
    public void usage() {
        // The `FastPrintf` instance should be created once and reused.
        FastPrintf fastPrintf = FastPrintf.compile("%#08X, %05.2f, %.5S");

        String format = fastPrintf.format(123456789L, Math.PI, "Hello World");
        assertEquals("0X75BCD15, 03.14, HELLO", format);

        Args args = Args.of(123456789L, Math.PI, "Hello World");
        assertEquals("0X75BCD15, 03.14, HELLO", fastPrintf.format(args));

        Args args1 = Args.create().putLong(123456789L).putDouble(Math.PI).putString("Hello World");
        assertEquals("0X75BCD15, 03.14, HELLO", fastPrintf.format(args1));
    }
}
```

## Spec

### Syntax

`%[flags][width][.precision]specifier`

### Specifiers

| specifier | Output                                                                                                                                                    | Example                    |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|
| d or i    | Signed decimal integer                                                                                                                                    | 392                        |
| u         | Unsigned decimal integer                                                                                                                                  | 7235                       |
| o         | Unsigned octal                                                                                                                                            | 610                        |
| x         | Unsigned hexadecimal integer                                                                                                                              | 7fa                        |
| X         | Unsigned hexadecimal integer (uppercase)                                                                                                                  | 7FA                        |
| f         | Decimal floating point, lowercase                                                                                                                         | 392.65                     |
| F         | Decimal floating point, uppercase                                                                                                                         | 392.65                     |
| e         | Scientific notation (mantissa/exponent), lowercase                                                                                                        | 3.9265e+2                  |
| E         | Scientific notation (mantissa/exponent), uppercase                                                                                                        | 3.9265E+2                  |
| g         | Use the shortest representation: %e or %f                                                                                                                 | 392.65                     |
| G         | Use the shortest representation: %E or %F                                                                                                                 | 392.65                     |
| a         | Hexadecimal floating point, lowercase                                                                                                                     | -0xc.90fep-2               |
| A         | Hexadecimal floating point, uppercase                                                                                                                     | -0XC.90FEP-2               |
| c         | Character                                                                                                                                                 | a                          |
| s         | String of characters                                                                                                                                      | sample                     |
| S         | String of characters, uppercase                                                                                                                           | SAMPLE                     |
| p         | Java Pointer address (Like `Object.toString()` output format)                                                                                             | b8000000                   |
| n         | Nothing printed. The corresponding argument must be a pointer to a signed int. The number of characters written so far is stored in the pointed location. | java.lang.Integer@707f7052 |
| %         | A % followed by another % character will write a single % to the stream. %                                                                                |                            |

### Flags

| flags   | description                                                                                                                                                                                                                                                                                                                                                                                                                                |
|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -       | Left align the result within the given field width.                                                                                                                                                                                                                                                                                                                                                                                        |
| +       | Forces to preceed the result with a plus or minus sign (+ or -) even for positive numbers. By default, only negative numbers are preceded with a - sign.                                                                                                                                                                                                                                                                                   |
| (space) | Use a blank to prefix the output value if it's signed and positive. The blank is ignored if both the blank and + flags appear.                                                                                                                                                                                                                                                                                                             |
| #       | When it's used with the o, x, or X format, the # flag uses 0, 0x, or 0X, respectively, to prefix any nonzero output value. When it's used with the e, E, f, F, a, or A format, the # flag forces the output value to contain a decimal point. When it's used with the g or G format, the # flag forces the output value to contain a decimal point and prevents the truncation of trailing zeros. Ignored when used with c, d, i, u, or s. |
| 0       | If width is prefixed by 0, leading zeros are added until the minimum width is reached. If both 0 and - appear, the 0 is ignored. If 0 is specified for an integer format (i, u, x, X, o, d) and a precision specification is also present—for example, %04.d—the 0 is ignored. If 0 is specified for the a or A floating-point format, leading zeros are prepended to the mantissa, after the 0x or 0X prefix.                             |

### Width

| width    | description                                                                                                                                                                                          |
|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| (number) | Minimum number of characters to be printed. If the value to be printed is shorter than this number, the result is padded with blank spaces. The value is not truncated even if the result is larger. |
| *        | The width is not specified in the format string, but as an additional integer value argument preceding the argument that has to be formatted.                                                        |

### Precision

| .precision | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| .number    | For integer specifiers (d, i, o, u, x, X): precision specifies the minimum number of digits to be written. If the value to be written is shorter than this number, the result is padded with leading zeros. The value is not truncated even if the result is longer. A precision of 0 means that no character is written for the value 0. For a, A, e, E, f and F specifiers: this is the number of digits to be printed after the decimal point (by default, this is 6). For g and G specifiers: This is the maximum number of significant digits to be printed. For s: this is the maximum number of characters to be printed. By default all characters are printed until the ending null character is encountered. If the period is specified without an explicit value for precision, 0 is assumed. |
| .*         | The precision is not specified in the format string, but as an additional integer value argument preceding the argument that has to be formatted.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
