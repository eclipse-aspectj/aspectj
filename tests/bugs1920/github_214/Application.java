public class Application {
  @MarkerTwo
  @MarkerOne
  public void greet(String name) {
    System.out.println("Hello " + name + "!");
  }

  public static void main(String[] args) {
    new Application().greet("world");
  }
}
