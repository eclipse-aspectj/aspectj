package spacewar;

 class C {
  
  public void m() {
    foo(12);
    foo(14);
  }

  public void foo(int i) {}
}

aspect X {
  before(): call(* foo(..)) {}
}

