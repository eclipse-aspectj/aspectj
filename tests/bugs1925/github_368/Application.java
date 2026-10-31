/**
 * https://github.com/eclipse-aspectj/aspectj/issues/368
 *
 * Regression test: weaving handler(Exception+) advice into a catch clause that
 * multi-catches an Exception subtype together with a non-Exception Throwable
 * subtype (e.g. "catch (Exception | LinkageError e)") used to produce a
 * VerifyError, because the caught variable's compile-time type in that case is
 * the least-upper-bound java.lang.Throwable, not Exception, and the woven
 * advice-dispatch code passed it on without a checkcast. Fixed since 1.9.21.2.
 */
public class Application {
  public static void main(String[] args) throws Exception {
    new Worker().doWork();
  }
}
