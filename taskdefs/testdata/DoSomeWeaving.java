class DoSomeWeaving {
  public static void main(String[] argv) {
    new DoSomeWeaving().sayHi();
  }


  public void sayHi() {
    System.err.println("hi");
  }
}

aspect A1 {
  before(): call(* *(..)) && !within(A1) {
    System.err.println("Just about to make a call");
  }
}
