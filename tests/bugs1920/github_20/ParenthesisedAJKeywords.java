/**
 * https://github.com/eclipse-aspectj/aspectj/issues/20
 */
public class ParenthesisedAJKeywords {
  public static void main(String[] args) {
    boolean before = true;
    int after = 11;
    String around = "around";
    boolean aspect = true;
    int pointcut = 22;
    String declare = "declare";
    String privileged = "privileged";

    if ((before)) {
      System.out.println(foo((before)));
      switch ((after)) {
        default: System.out.println("after");
      }
      System.out.println((around));
      System.out.println(!(aspect) ? "no aspect" : "aspect");
      switch ((pointcut)) {
        case 22: System.out.println("pointcut"); break;
        default: System.out.println("xxx");
      }
      System.out.println((declare));
      System.out.println((privileged));
    }
  }

  public static String foo(boolean before) {
    return (before) ? "before" : "after";
  }
}
