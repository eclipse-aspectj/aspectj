// TESTING: Factory method directly takes the type specified in the Mixin target (strongly typed)
import org.aspectj.lang.annotation.*;

public class CaseF {
  public static void main(String[]argv) {
    CaseF cc = new CaseF();
    ((I)cc).methodOne(); // will only succeed if mixin applied
  }

  public String toString() {
    return "CaseF instance";
  }
}

aspect X {
  @DeclareMixin("CaseF")
  public static I createImplementation(CaseF cf) {
    System.out.println("Delegate factory invoked for "+cf.toString());
    return new Implementation(cf);
  }
}

interface I {
  void methodOne();
}

class Implementation implements I {
  public Implementation(CaseF cf) {}
  public void methodOne() {
    System.out.println("methodOne running");
  }
}
