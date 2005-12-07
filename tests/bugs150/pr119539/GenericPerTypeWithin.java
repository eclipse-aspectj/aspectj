package bugs;

public class GenericPerTypeWithin {
  public static void main(String[] args) {
    new C();
  }
}

class C {
  C() {}
}
class B {
  B() {}	
}


abstract aspect Singleton<Target> pertypewithin(Target) {
    pointcut creation() : execution(Target+.new()) ;
    pointcut creationOfAnything(): execution(*.new());
    before() : creation() {  }
    before() : creationOfAnything() {} // should not match B.new() because of the ptw
}

aspect A extends Singleton<C> {}
