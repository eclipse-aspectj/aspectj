import java.nio.charset.Charset;
import sun.nio.cs.ext.ExtendedCharsets;

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
