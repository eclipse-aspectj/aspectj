// TESTING: factory method takes the object for which the delegate exists
import org.aspectj.lang.annotation.*;

public class CaseC {
  public static void main(String[]argv) {
    CaseC cc = new CaseC();
    ((I)cc).methodOne(); // will only succeed if mixin applied
  }

  public String toString() {
    return "CaseC instance";
  }
}

aspect X {
  @DeclareMixin("CaseC")
  public static I createImplementation(Object o) {
    System.out.println("Delegate factory invoked for "+o.toString());
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
