// TESTING: factory returns class but interface specified - not ok as class doesn't implement interface
import org.aspectj.lang.annotation.*;

public class CaseO {
  public static void main(String[]argv) { 
    ((I)new CaseO()).foo();
  }
}

aspect X {
  @DeclareMixin(value="CaseO",interfaces={I.class})
  public static C createImplementation1() {return new C();}

}

interface I {
  void foo();
}

class C {
  void foo() {
    System.out.println("foo() running");
  }
}
