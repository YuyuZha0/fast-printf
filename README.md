## fast-printf

`fast-printf` is a Java library for fast printf-like formatting. Features:

* Can be extremely fast , about 4x faster than `String.format`.
* Compatible with GLibC printf format.
*

## Usage

```java
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