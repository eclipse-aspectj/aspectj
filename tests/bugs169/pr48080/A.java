public class A {
  public void m() {
  }
}

aspect X {
  declare warning: execution(* A.m(..)): "method found";
}
