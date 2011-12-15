public class LiteralsITD {
  public static void main(String []argv) {
  }
}

aspect Foo {
  before(): execution(* *(..)) {
  }

  public int LiteralsITD.id = 0b100_000;
}
