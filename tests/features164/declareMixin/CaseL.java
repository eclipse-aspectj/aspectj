// TESTING: mixin of a class - should be an error
import org.aspectj.lang.annotation.*;

public class CaseL {
  public static void main(String[]argv) { 
    ((C)new CaseL()).foo();
  }
}

aspect X {
  @DeclareMixin("CaseL")
  public static C createImplementation1() {return new C();}

}

class C {
  void foo() {
    System.out.println("foo() running");
  }
}
