import sun.security.x509.X509CertInfo;

import java.security.cert.CertificateParsingException;

/**
 * https://github.com/mojohaus/aspectj-maven-plugin/issues/139
 */
public class Application {
  public static void main(String[] args) {
    try {
      new X509CertInfo(new byte[0]);
    }
    catch (CertificateParsingException e) {
      System.out.println(e);
    }
  }
}
