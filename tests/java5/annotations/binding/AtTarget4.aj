import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Inherited @interface Colored { String color(); }

public class AtTarget4 {
  public static void main(String[]argv) {
    new A().m();
    new B().m();
    new C().m();
  }
}

@Colored(color="yellow")
class A {
  public void m() { System.err.println("method"); }
}

class B extends A { } // inherits yellow color :)

@Colored(color="blue") class C extends B { }

aspect X {
  int adviceexecutions = 0;

  before(Colored c): call(* *(..)) && !within(X) && @target(c) {
    System.err.println(c.color() + thisJoinPoint);
    adviceexecutions++;
    if (adviceexecutions == 1 && !c.color().equals("yellow"))
      throw new RuntimeException("First advice execution, color should be yellow");
    
    if (adviceexecutions == 2 && !c.color().equals("yellow"))
      throw new RuntimeException("Second advice execution, color should be yellow");
 
    if (adviceexecutions == 3 && !c.color().equals("blue"))
        throw new RuntimeException("Third advice execution, color should be blue");
   
    if (adviceexecutions > 3) 
      throw new RuntimeException("Advice should only run twice");
  }
}

