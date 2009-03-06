// TESTING: mixin of a class - should be an error
import org.aspectj.lang.annotation.*;

public class CaseM {
  public static void main(String[]argv) { 
  }
}

aspect X {
  @DeclareMixin("CaseM")
  public static C createImplementation1() {return new C();}

}

class C {
  void foo() {
    System.out.println("foo() running");
  }
}
