// should be ok...

public abstract aspect GenericAspect3<S,T> {

  public void doSomething(S s,T t) { 
    System.err.println(s);
    System.err.println(t);
  }

  public static void main(String[]argv) {
    m();
  }

  public static void m() {}

}

aspect Sub extends GenericAspect3<String,String> { 

  before(): call(* m(..)) {
    doSomething("A","B");
  }

}