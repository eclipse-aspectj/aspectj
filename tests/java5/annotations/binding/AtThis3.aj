import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Colored { String color(); }

public class AtThis3 {
  public static void main(String[]argv) {
    new A().start();
    new B().start();
    new C().start();
    X.verify();
  }
}

@Colored(color="yellow")
class A {
  public void start() { m();}
  public void m() { System.err.println("method"); }
}

class B extends A { }

@Colored(color="blue") class C extends B { }

aspect X {
  static int count = 0;

  before(Colored c): call(* m(..)) && !within(X) && @this(c) {
    System.err.println(c.color());
    count++;
    if (count == 1 && !c.color().equals("yellow"))
      throw new RuntimeException("First advice execution, color should be yellow");
    
    // The pointcut does 'match' on call to B.m() but at runtime the
    // annotation check fails so the advice isn't run

    if (count == 2 && !c.color().equals("blue"))
      throw new RuntimeException("Second advice execution, color should be blue");
  }

 public static void verify() {
    if (count!=2) throw new Error("Should be 2 runs: "+count);
  }
}

