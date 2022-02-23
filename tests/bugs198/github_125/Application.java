public class Application {
  public static void main(String[] argv) {
    System.out.println("Hello world!");
  }

  static aspect MyAspect {
    before(): execution(* Application.main(..)) {
      System.out.println("Before advice");
    }
  }
}
