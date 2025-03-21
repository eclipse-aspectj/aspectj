aspect A {
  private void priv(String a) { }
  Object around(String s): execution(* foo(..)) && args(s) {
    priv(s);
    return null;
  }
}

class C {
  public void foo(String f) { }
}
