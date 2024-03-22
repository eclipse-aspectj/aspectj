public class Invoker {
  public static void main(String[] args) throws Throwable {
    try {
      new C();
    }
    catch (Throwable t) {
      boolean failedCorrectly =
        t.toString().contains("Unresolved compilation problem") &&
          t.toString().contains("The method main cannot be declared static");
      if (failedCorrectly)
        return;
      throw new RuntimeException("Constructor call should have failed!", t);
    }
  }
}
