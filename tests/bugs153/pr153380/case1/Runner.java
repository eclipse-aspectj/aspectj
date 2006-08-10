public class Runner {
  public static void main(String []argv) {
    new BaseImpl().m();
  }

  static aspect A{
    before(): call(* *(..)) {}
  }
}
