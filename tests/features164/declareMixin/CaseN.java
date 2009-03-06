// TESTING: factory returns class but interface specified - this is OK
import org.aspectj.lang.annotation.*;

public class CaseN {
  public static void main(String[]argv) { 
    ((I)new CaseN()).foo();
  }
}

aspect X {
  @DeclareMixin(value="CaseN",interfaces={I.class})
  public static C createImplementation1() {return new C();}

}

interface I {
  void foo();
}

class C implements I {
  public void foo() {
    System.out.println("foo() running");
  }
}
