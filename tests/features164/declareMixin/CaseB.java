import org.aspectj.lang.annotation.*;

public class CaseB {
  public static void main(String[]argv) {
    CaseB cb = new CaseB();
    ((I)cb).methodOne(); // will only succeed if mixin applied
  }
}

aspect X {
  // TESTING: non static factory method, will need aspectOf() calling on 
  // the aspect before the factory is called
  @DeclareMixin("CaseB")
  public I createImplementation() {
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
