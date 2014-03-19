public aspect Code {
  declare warning: call(* foo(..)): "Call to foo made inside class {joinpoint.enclosingclass}";
  declare warning: call(* foo(..)): "Call to foo made inside member {joinpoint.enclosingmember.name}";
  declare warning: call(* foo(..)): "Call to foo made inside member {joinpoint.enclosingmember}";
}
class Bar {
  public void booble() {
    foo();
  }
  public void foo() {}
}

class Boo {
  public void m() {
    foo();
  }
  public void foo() {}
}
