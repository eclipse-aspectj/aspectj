public class Good {

  public void foo() {
    int i = 0;
    i += 1;
    i += 2;
  }

  int x = 3;
}

aspect A {
  before(): call(* *(..)) {
    System.out.println("");
  }
}


