// TESTING: Very basics with a simple static factory method
import org.aspectj.lang.annotation.*;
 
public class CaseA {
  public static void main(String[]argv) {
    CaseA ca = new CaseA();
    ((I)ca).methodOne(); // will only succeed if mixin applied
  }
}

aspect X {
  @DeclareMixin("CaseA")
  public static I createImplementation() {
    System.out.println("Delegate factory invoked");
    return new Implementation();
  }
}

interface I {
  void methodOne();
}

class Implementation implements I {
  public void methodOne() {
    System.out.println("methodOne running");
  }
}
