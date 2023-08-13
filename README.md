## fast-printf

`fast-printf` is a Java library for fast printf-like formatting. Features:

* Can be extremely fast , about 4x faster than `String.format`.
* Compatible with `glibc` printf format, rather than Java `String.format` format.
* Zero dependency. No external dependencies, requires only Java 8+.

## Usage

```xml
<dependency>
    <groupId>io.github.yuyuzha0</groupId>
    <artifactId>fast-printf</artifactId>
    <version>0.9.0</version>
</dependency>
```

```java
import org.fastprintf.Args;
import org.fastprintf.FastPrintf;

public class Demo {
    @Test
    public void test() {
        FastPrintf fastPrintf = FastPrintf.compile("floats: %4.2f %+.0e %E \n");
        Args args = Args.of(3.1416, 3.1416, 3.1416);
        String format = fastPrintf.format(args);
        assertEquals("floats: 3.14 +3e+00 3.141600E+00 \n", format);
    }
}
```