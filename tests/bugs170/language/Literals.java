public class Literals {
  public static void main(String []argv) {
  }
}

aspect Foo {
  before(): execution(* *(..)) {
    int onemill = 1_000_000;
    int four =0b100;
  }
  

}
