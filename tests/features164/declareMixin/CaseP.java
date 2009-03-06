// TESTING: interface subsetting used (factory returns class) - but only one method should be delegated
import org.aspectj.lang.annotation.*;

public class CaseP {
  public static void main(String[]argv) { 
    ((I)new CaseP()).foo();
    CaseP cp = new CaseP();
    if (cp instanceof J) { // should not have been mixed in
      throw new RuntimeException();
    }
  }
}

aspect X {
  @DeclareMixin(value="CaseP",interfaces={I.class})
  public static C createImplementation1() {return new C();}

}

interface I {
  void foo();
}

interface J {
  void goo();
}

class C implements I,J {
  public void foo() {
    System.out.println("foo() running");
  }
  public void goo() {
    System.out.println("goo() running");
  }
}
