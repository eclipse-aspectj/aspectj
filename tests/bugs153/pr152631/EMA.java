
public aspect EMA {
    before() : cflow(execution(* *(..))) {}
}

aspect Goo {
  declare parents: EMA extends C;
  public void EMA.m() {}
}

abstract class C {
  abstract void m();
}
