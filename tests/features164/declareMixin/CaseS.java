// TESTING: factory method has incompatible return type - verifyerror if we did use that factory
import org.aspectj.lang.annotation.*;

public class CaseS {
  public static void main(String[]argv) {
    CaseS cc = new CaseS();
    ((I)cc).methodOne(); // will only succeed if mixin applied
  }

  public String toString() {
    return "CaseS instance";
  }
}

aspect X {
  @DeclareMixin("CaseS")
  public static I createImplementation(FooI cf) {
	System.out.println(cf instanceof FooI);
    System.out.println("Delegate factory invoked for "+cf.toString());
    return new Implementation();
  }
}

class FooI {
	
}

interface I {
  void methodOne();
}

class Implementation implements I {
  public void methodOne() {
    System.out.println("methodOne running");
  }
}
