import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Inherited @interface Colored { String color(); }

public class AtThis4 {
  public static void main(String[]argv) {
    new A().start();
    new B().start();
    new C().start();
    X.verify();
  }
}

@Colored(color="yellow")
class A {
  public void start() { m();} // Can't match @this() on calls from static method, 
                              // hence this start() method...
  public void m() { System.err.println("method"); }
}

class B extends A { } // inherits yellow color :)

@Colored(color="blue") class C extends B { }

aspect X {
  static int count = 0;

  before(Colored c): call(* m(..)) && !within(X) && @this(c) {
    System.err.println(c.color() + thisJoinPoint);
    count++;
    if (count == 1 && !c.color().equals("yellow"))
      throw new RuntimeException("First advice execution, color should be yellow");
    
    if (count == 2 && !c.color().equals("yellow"))
      throw new RuntimeException("Second advice execution, color should be yellow");
 
    if (count == 3 && !c.color().equals("blue"))
        throw new RuntimeException("Third advice execution, color should be blue");
   
  }

   public static void verify() {
    if (count!=3) throw new Error("Should be 3 runs: "+count);
  }
}

