// TESTING: testing a pure marker interface - no methods added
import org.aspectj.lang.annotation.*;

public class CaseT {
  public static void main(String[]argv) { 
	  CaseT ct = new CaseT();
	  System.out.println(ct instanceof I);
	  System.out.println(ct instanceof J);
  }
}

aspect X {
  @DeclareMixin(value="CaseT",interfaces={I.class})
  public static C createImplementation1() {return null;}
}

interface I {}
interface J {}

class C implements I,J {
  public void foo() {
    System.out.println("foo() running");
  }
  public void goo() {
    System.out.println("goo() running");
  }
}
