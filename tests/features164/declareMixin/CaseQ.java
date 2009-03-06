// TESTING: factory return type implements two interfaces, both should be mixed as specified
import org.aspectj.lang.annotation.*;

public class CaseQ {
  public static void main(String[]argv) { 
    ((I)new CaseQ()).foo();
    ((J)new CaseQ()).goo();
  }
}

aspect X {
  @DeclareMixin(value="CaseQ",interfaces={I.class,J.class})
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
