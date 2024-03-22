public class Invoker3 {
  public static void main(String[] args) throws Throwable {
    try {
      C3.main(null);
    }
    catch (Throwable t) {
      boolean failedCorrectly = t.toString().indexOf("Unresolved compilation") != -1;
      if (failedCorrectly)
        return;
      throw new RuntimeException("Call to main should have failed!", t);
    }
    try {
      new C3();
    }
    catch (Throwable t) {
      boolean failedCorrectly =
        t.toString().contains("Unresolved compilation problem") &&
          t.toString().contains("blahblahpackage cannot be resolved to a type");
      if (failedCorrectly)
        return;
      throw new RuntimeException("Constructor call should have failed!", t);
    }
  }
}
