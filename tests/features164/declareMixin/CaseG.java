// TESTING: targeting multiple types from the Mixin
import org.aspectj.lang.annotation.*;

public class CaseG {
  public static void main(String[]argv) {
    ((I)new A()).run();
    ((I)new B()).run();
  }

}

class A {
}
class B {
}

aspect X {
  @DeclareMixin("A || B")
  public I createImplementation(Object o) {
    System.out.println("Delegate factory invoked for instance of "+o.getClass().getSimpleName());
    return new Implementation(o);
  }
}

interface I {
  void run();
}

class Implementation implements I {
  Object o;

  public Implementation(Object o) {
    this.o = o;
  }

  public void run() {
    System.out.println("run() executing on behalf of "+o.getClass().getSimpleName());
  }
}
