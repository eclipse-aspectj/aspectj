interface MyBase { void foo(); };
interface MyMarker extends MyBase { void bar(); }

abstract aspect Base<A extends MyBase> {

 pointcut somePC() : execution(* A.*(..));

 declare warning : somePC() : "a match";

}

abstract aspect Middle<B extends MyBase> extends Base<B> {}

aspect Sub extends Middle<MyMarker> {}
         
         
class C1 implements MyBase {

  public void foo() {}

}

class C2 implements MyMarker {

  public void foo() {}  // CW L 25
  
  public void bar() {}  // CW L 27


}