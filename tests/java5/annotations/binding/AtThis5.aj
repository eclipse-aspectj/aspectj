import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Inherited @interface Colored { String color(); }

public class AtThis5 {
  public static void main(String[]argv) {
    new A().start(new B());
    new B().start(new C());
    new C().start(new A());
    X.verify();
  }
}

@Colored(color="yellow")
class A {
  public void start(A a) { a.m();} // Can't match @this() on calls from static method, 
                              // hence this start() method...
  public void m() { System.err.println("method"); }
}

class B extends A { } // inherits yellow color :)

@Colored(color="blue") class C extends B { }

aspect X {
  static int count = 0;

  before(Colored cThis,Colored cTarg): 
    call(* m(..)) && !within(X) && @this(cThis) && @target(cTarg) {

    count++;
    if (count == 1 && 
        !(cThis.color().equals("yellow") && cTarg.color().equals("yellow")))
      throw new RuntimeException("1st run, colors should be yellow:yellow but are "+
        cThis.color()+":"+cTarg.color());
    
    if (count == 2 &&
        !(cThis.color().equals("yellow") && cTarg.color().equals("blue")))
      throw new RuntimeException("2nd run, colors should be yellow:blue but are "+
        cThis.color()+":"+cTarg.color());
 
    if (count == 3 && 
        !(cThis.color().equals("blue") && cTarg.color().equals("yellow")))
      throw new RuntimeException("3rd run, colors should be blue:yellow but are "+
        cThis.color()+":"+cTarg.color());
   
    if (count > 3) 
      throw new RuntimeException("Advice should only run three times");
  }

  public static void verify() {
    if (count!=3) throw new Error("Should be 3 runs: "+count);
  }
}

