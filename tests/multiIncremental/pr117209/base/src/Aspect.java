public class Aspect {
  public static void main(String []argv) {
    new Aspect().m();
  }

  public void m() {
  }
}

aspect Y {

  public int Aspect.x = 5;
  public void Aspect.foo() {
  }
  before():execution(* m()) {
  }

  before(): staticinitialization(*) {

  }
}
