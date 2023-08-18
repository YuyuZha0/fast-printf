## fast-printf

`fast-printf` is a Java library for fast printf-like formatting. Features:

* Can be extremely fast , about **4x** faster than `String.format`.
* Compatible with `glibc` printf format, rather than Java `String.format` format.
* Zero dependency. No external dependencies, requires only Java 8+.

## Usage

```xml

<dependency>
    <groupId>io.github.yuyuzha0</groupId>
    <artifactId>fast-printf</artifactId>
    <version>0.9.1</version>
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