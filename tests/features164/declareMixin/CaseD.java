// TESTING: factory method is non static and takes the object for which the delegate is being created
import org.aspectj.lang.annotation.*;

public class CaseD {
  public static void main(String[]argv) {
    CaseD cd = new CaseD();
    ((I)cd).methodOne(); // will only succeed if mixin applied
  }

  public String toString() {
    return "CaseD instance";
  }
}

aspect X {
  @DeclareMixin("CaseD")
  public I createImplementation(Object o) {
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
