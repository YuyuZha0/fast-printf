package io.fastprintf;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

// https://github.com/BartMassey/printf-tests/blob/master/sources/tests-libc-sprintf.c
public class LibcCasesTest {

  private static void TEST(String result, int length, String format, Object... values) {
    assertEquals(length, result.length());
    String s = FastPrintf.compile(format).format(values);
    assertEquals(
        String.format("Format: \"%s\" Args: %s", format, Arrays.toString(values)), result, s);
  }

  @Test
  public void test() {
    /* Ein String ohne alles */
    TEST("Hallo heimur", 12, "Hallo heimur");

    /* Einfache Konvertierungen */
    TEST("Hallo heimur", 12, "%s", "Hallo heimur");
    TEST("1024", 4, "%d", 1024);
    TEST("-1024", 5, "%d", -1024);
    TEST("1024", 4, "%i", 1024);
    TEST("-1024", 5, "%i", -1024);
    TEST("1024", 4, "%u", 1024);
    TEST("4294966272", 10, "%u", -1024);
    TEST("777", 3, "%o", 0777);
    TEST("37777777001", 11, "%o", -0777);
    TEST("1234abcd", 8, "%x", 0x1234abcd);
    TEST("edcb5433", 8, "%x", -0x1234abcd);
    TEST("1234ABCD", 8, "%X", 0x1234abcd);
    TEST("EDCB5433", 8, "%X", -0x1234abcd);
    TEST("x", 1, "%c", 'x');
    TEST("%", 1, "%%");

    /* Mit %c kann man auch Nullbytes ausgeben */
    TEST("\0", 1, "%c", '\0');

    /* Vorzeichen erzwingen (Flag +) */
    TEST("Hallo heimur", 12, "%+s", "Hallo heimur");
    TEST("+1024", 5, "%+d", 1024);
    TEST("-1024", 5, "%+d", -1024);
    TEST("+1024", 5, "%+i", 1024);
    TEST("-1024", 5, "%+i", -1024);
    TEST("1024", 4, "%+u", 1024);
    TEST("4294966272", 10, "%+u", -1024);
    TEST("777", 3, "%+o", 0777);
    TEST("37777777001", 11, "%+o", -0777);
    TEST("1234abcd", 8, "%+x", 0x1234abcd);
    TEST("edcb5433", 8, "%+x", -0x1234abcd);
    TEST("1234ABCD", 8, "%+X", 0x1234abcd);
    TEST("EDCB5433", 8, "%+X", -0x1234abcd);
    TEST("x", 1, "%+c", 'x');

    /* Vorzeichenplatzhalter erzwingen (Flag <space>) */
    TEST("Hallo heimur", 12, "% s", "Hallo heimur");
    TEST(" 1024", 5, "% d", 1024);
    TEST("-1024", 5, "% d", -1024);
    TEST(" 1024", 5, "% i", 1024);
    TEST("-1024", 5, "% i", -1024);
    TEST("1024", 4, "% u", 1024);
    TEST("4294966272", 10, "% u", -1024);
    TEST("777", 3, "% o", 0777);
    TEST("37777777001", 11, "% o", -0777);
    TEST("1234abcd", 8, "% x", 0x1234abcd);
    TEST("edcb5433", 8, "% x", -0x1234abcd);
    TEST("1234ABCD", 8, "% X", 0x1234abcd);
    TEST("EDCB5433", 8, "% X", -0x1234abcd);
    TEST("x", 1, "% c", 'x');

    /* Flag + hat Vorrang über <space> */
    TEST("Hallo heimur", 12, "%+ s", "Hallo heimur");
    TEST("+1024", 5, "%+ d", 1024);
    TEST("-1024", 5, "%+ d", -1024);
    TEST("+1024", 5, "%+ i", 1024);
    TEST("-1024", 5, "%+ i", -1024);
    TEST("1024", 4, "%+ u", 1024);
    TEST("4294966272", 10, "%+ u", -1024);
    TEST("777", 3, "%+ o", 0777);
    TEST("37777777001", 11, "%+ o", -0777);
    TEST("1234abcd", 8, "%+ x", 0x1234abcd);
    TEST("edcb5433", 8, "%+ x", -0x1234abcd);
    TEST("1234ABCD", 8, "%+ X", 0x1234abcd);
    TEST("EDCB5433", 8, "%+ X", -0x1234abcd);
    TEST("x", 1, "%+ c", 'x');

    /* Alternative Form */
    TEST("0777", 4, "%#o", 0777);
    TEST("037777777001", 12, "%#o", -0777);
    TEST("0x1234abcd", 10, "%#x", 0x1234abcd);
    TEST("0xedcb5433", 10, "%#x", -0x1234abcd);
    TEST("0X1234ABCD", 10, "%#X", 0x1234abcd);
    TEST("0XEDCB5433", 10, "%#X", -0x1234abcd);
    TEST("0", 1, "%#o", 0);
    TEST("0", 1, "%#x", 0);
    TEST("0", 1, "%#X", 0);

    /* Feldbreite: Kleiner als Ausgabe */
    TEST("Hallo heimur", 12, "%1s", "Hallo heimur");
    TEST("1024", 4, "%1d", 1024);
    TEST("-1024", 5, "%1d", -1024);
    TEST("1024", 4, "%1i", 1024);
    TEST("-1024", 5, "%1i", -1024);
    TEST("1024", 4, "%1u", 1024);
    TEST("4294966272", 10, "%1u", -1024);
    TEST("777", 3, "%1o", 0777);
    TEST("37777777001", 11, "%1o", -0777);
    TEST("1234abcd", 8, "%1x", 0x1234abcd);
    TEST("edcb5433", 8, "%1x", -0x1234abcd);
    TEST("1234ABCD", 8, "%1X", 0x1234abcd);
    TEST("EDCB5433", 8, "%1X", -0x1234abcd);
    TEST("x", 1, "%1c", 'x');

    /* Feldbreite: Größer als Ausgabe */
    TEST("               Hallo", 20, "%20s", "Hallo");
    TEST("                1024", 20, "%20d", 1024);
    TEST("               -1024", 20, "%20d", -1024);
    TEST("                1024", 20, "%20i", 1024);
    TEST("               -1024", 20, "%20i", -1024);
    TEST("                1024", 20, "%20u", 1024);
    TEST("          4294966272", 20, "%20u", -1024);
    TEST("                 777", 20, "%20o", 0777);
    TEST("         37777777001", 20, "%20o", -0777);
    TEST("            1234abcd", 20, "%20x", 0x1234abcd);
    TEST("            edcb5433", 20, "%20x", -0x1234abcd);
    TEST("            1234ABCD", 20, "%20X", 0x1234abcd);
    TEST("            EDCB5433", 20, "%20X", -0x1234abcd);
    TEST("                   x", 20, "%20c", 'x');

    /* Feldbreite: Linksbündig */
    TEST("Hallo               ", 20, "%-20s", "Hallo");
    TEST("1024                ", 20, "%-20d", 1024);
    TEST("-1024               ", 20, "%-20d", -1024);
    TEST("1024                ", 20, "%-20i", 1024);
    TEST("-1024               ", 20, "%-20i", -1024);
    TEST("1024                ", 20, "%-20u", 1024);
    TEST("4294966272          ", 20, "%-20u", -1024);
    TEST("777                 ", 20, "%-20o", 0777);
    TEST("37777777001         ", 20, "%-20o", -0777);
    TEST("1234abcd            ", 20, "%-20x", 0x1234abcd);
    TEST("edcb5433            ", 20, "%-20x", -0x1234abcd);
    TEST("1234ABCD            ", 20, "%-20X", 0x1234abcd);
    TEST("EDCB5433            ", 20, "%-20X", -0x1234abcd);
    TEST("x                   ", 20, "%-20c", 'x');

    /* Feldbreite: Padding mit 0 */
    TEST("00000000000000001024", 20, "%020d", 1024);
    TEST("-0000000000000001024", 20, "%020d", -1024);
    TEST("00000000000000001024", 20, "%020i", 1024);
    TEST("-0000000000000001024", 20, "%020i", -1024);
    TEST("00000000000000001024", 20, "%020u", 1024);
    TEST("00000000004294966272", 20, "%020u", -1024);
    TEST("00000000000000000777", 20, "%020o", 0777);
    TEST("00000000037777777001", 20, "%020o", -0777);
    TEST("0000000000001234abcd", 20, "%020x", 0x1234abcd);
    TEST("000000000000edcb5433", 20, "%020x", -0x1234abcd);
    TEST("0000000000001234ABCD", 20, "%020X", 0x1234abcd);
    TEST("000000000000EDCB5433", 20, "%020X", -0x1234abcd);

    /* Feldbreite: Padding und alternative Form */
    TEST("                0777", 20, "%#20o", 0777);
    TEST("        037777777001", 20, "%#20o", -0777);
    TEST("          0x1234abcd", 20, "%#20x", 0x1234abcd);
    TEST("          0xedcb5433", 20, "%#20x", -0x1234abcd);
    TEST("          0X1234ABCD", 20, "%#20X", 0x1234abcd);
    TEST("          0XEDCB5433", 20, "%#20X", -0x1234abcd);

    TEST("00000000000000000777", 20, "%#020o", 0777);
    TEST("00000000037777777001", 20, "%#020o", -0777);
    TEST("0x00000000001234abcd", 20, "%#020x", 0x1234abcd);
    TEST("0x0000000000edcb5433", 20, "%#020x", -0x1234abcd);
    TEST("0X00000000001234ABCD", 20, "%#020X", 0x1234abcd);
    TEST("0X0000000000EDCB5433", 20, "%#020X", -0x1234abcd);

    /* Feldbreite: - hat Vorrang vor 0 */
    TEST("Hallo               ", 20, "%0-20s", "Hallo");
    TEST("1024                ", 20, "%0-20d", 1024);
    TEST("-1024               ", 20, "%0-20d", -1024);
    TEST("1024                ", 20, "%0-20i", 1024);
    TEST("-1024               ", 20, "%0-20i", -1024);
    TEST("1024                ", 20, "%0-20u", 1024);
    TEST("4294966272          ", 20, "%0-20u", -1024);
    TEST("777                 ", 20, "%-020o", 0777);
    TEST("37777777001         ", 20, "%-020o", -0777);
    TEST("1234abcd            ", 20, "%-020x", 0x1234abcd);
    TEST("edcb5433            ", 20, "%-020x", -0x1234abcd);
    TEST("1234ABCD            ", 20, "%-020X", 0x1234abcd);
    TEST("EDCB5433            ", 20, "%-020X", -0x1234abcd);
    TEST("x                   ", 20, "%-020c", 'x');

    /* Feldbreite: Aus Parameter */
    TEST("               Hallo", 20, "%*s", 20, "Hallo");
    TEST("                1024", 20, "%*d", 20, 1024);
    TEST("               -1024", 20, "%*d", 20, -1024);
    TEST("                1024", 20, "%*i", 20, 1024);
    TEST("               -1024", 20, "%*i", 20, -1024);
    TEST("                1024", 20, "%*u", 20, 1024);
    TEST("          4294966272", 20, "%*u", 20, -1024);
    TEST("                 777", 20, "%*o", 20, 0777);
    TEST("         37777777001", 20, "%*o", 20, -0777);
    TEST("            1234abcd", 20, "%*x", 20, 0x1234abcd);
    TEST("            edcb5433", 20, "%*x", 20, -0x1234abcd);
    TEST("            1234ABCD", 20, "%*X", 20, 0x1234abcd);
    TEST("            EDCB5433", 20, "%*X", 20, -0x1234abcd);
    TEST("                   x", 20, "%*c", 20, 'x');

    /* Präzision / Mindestanzahl von Ziffern */
    TEST("Hallo heimur", 12, "%.20s", "Hallo heimur");
    TEST("00000000000000001024", 20, "%.20d", 1024);
    TEST("-00000000000000001024", 21, "%.20d", -1024);
    TEST("00000000000000001024", 20, "%.20i", 1024);
    TEST("-00000000000000001024", 21, "%.20i", -1024);
    TEST("00000000000000001024", 20, "%.20u", 1024);
    TEST("00000000004294966272", 20, "%.20u", -1024);
    TEST("00000000000000000777", 20, "%.20o", 0777);
    TEST("00000000037777777001", 20, "%.20o", -0777);
    TEST("0000000000001234abcd", 20, "%.20x", 0x1234abcd);
    TEST("000000000000edcb5433", 20, "%.20x", -0x1234abcd);
    TEST("0000000000001234ABCD", 20, "%.20X", 0x1234abcd);
    TEST("000000000000EDCB5433", 20, "%.20X", -0x1234abcd);

    /* Feldbreite und Präzision */
    TEST("               Hallo", 20, "%20.5s", "Hallo heimur");
    TEST("               01024", 20, "%20.5d", 1024);
    TEST("              -01024", 20, "%20.5d", -1024);
    TEST("               01024", 20, "%20.5i", 1024);
    TEST("              -01024", 20, "%20.5i", -1024);
    TEST("               01024", 20, "%20.5u", 1024);
    TEST("          4294966272", 20, "%20.5u", -1024);
    TEST("               00777", 20, "%20.5o", 0777);
    TEST("         37777777001", 20, "%20.5o", -0777);
    TEST("            1234abcd", 20, "%20.5x", 0x1234abcd);
    TEST("          00edcb5433", 20, "%20.10x", -0x1234abcd);
    TEST("            1234ABCD", 20, "%20.5X", 0x1234abcd);
    TEST("          00EDCB5433", 20, "%20.10X", -0x1234abcd);

    /* Präzision: 0 wird ignoriert */
    TEST("               Hallo", 20, "%020.5s", "Hallo heimur");
    TEST("               01024", 20, "%020.5d", 1024);
    TEST("              -01024", 20, "%020.5d", -1024);
    TEST("               01024", 20, "%020.5i", 1024);
    TEST("              -01024", 20, "%020.5i", -1024);
    TEST("               01024", 20, "%020.5u", 1024);
    TEST("          4294966272", 20, "%020.5u", -1024);
    TEST("               00777", 20, "%020.5o", 0777);
    TEST("         37777777001", 20, "%020.5o", -0777);
    TEST("            1234abcd", 20, "%020.5x", 0x1234abcd);
    TEST("          00edcb5433", 20, "%020.10x", -0x1234abcd);
    TEST("            1234ABCD", 20, "%020.5X", 0x1234abcd);
    TEST("          00EDCB5433", 20, "%020.10X", -0x1234abcd);

    /* Präzision 0 */
    TEST("", 0, "%.0s", "Hallo heimur");
    TEST("                    ", 20, "%20.0s", "Hallo heimur");
    TEST("", 0, "%.s", "Hallo heimur");
    TEST("                    ", 20, "%20.s", "Hallo heimur");
    TEST("                1024", 20, "%20.0d", 1024);
    TEST("               -1024", 20, "%20.d", -1024);
    TEST("                    ", 20, "%20.d", 0);
    TEST("                1024", 20, "%20.0i", 1024);
    TEST("               -1024", 20, "%20.i", -1024);
    TEST("                    ", 20, "%20.i", 0);
    TEST("                1024", 20, "%20.u", 1024);
    TEST("          4294966272", 20, "%20.0u", -1024);
    TEST("                    ", 20, "%20.u", 0);
    TEST("                 777", 20, "%20.o", 0777);
    TEST("         37777777001", 20, "%20.0o", -0777);
    TEST("                    ", 20, "%20.o", 0);
    TEST("            1234abcd", 20, "%20.x", 0x1234abcd);
    TEST("            edcb5433", 20, "%20.0x", -0x1234abcd);
    TEST("                    ", 20, "%20.x", 0);
    TEST("            1234ABCD", 20, "%20.X", 0x1234abcd);
    TEST("            EDCB5433", 20, "%20.0X", -0x1234abcd);
    TEST("                    ", 20, "%20.X", 0);
  }
}
