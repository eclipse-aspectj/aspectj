import java.nio.charset.Charset;
import sun.nio.cs.ext.ExtendedCharsets;

/**
 * Note that the Windows JDK knows many more extended charsets than the Linux one. Originally, this test was using
 * "hebrew", but that yielded failing GitHub CI tests due to the unavailability of the character set there. I actually
 * had to inspect a Linux 'lib/modules' file using 'jimage' in order to filter for available 'ISO*' classes in the
 * extended character set package. So if this test ever breaks again on any OS platform, you know where to look for the
 * root cause and have a clue how to fix the test.
 */
public class UseJDKExtendedCharsets {
  static ExtendedCharsets charsets = new ExtendedCharsets();
  static Charset iso2022jp = charsets.charsetForName("ISO-2022-JP");
  static Charset jis = charsets.charsetForName("jis");
  static Charset jis_encoding = charsets.charsetForName("jis_encoding");

  public static void main(String[] args) {
    // The 3 charsets are aliases of each other
    assert iso2022jp != null;
    System.out.println(iso2022jp);
    assert iso2022jp.equals(jis);
    assert iso2022jp.equals(jis_encoding);
  }
}
