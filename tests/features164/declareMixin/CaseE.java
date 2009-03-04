// TESTING: multiple instances causing factory invocation multiple times (but is cached)
import org.aspectj.lang.annotation.*;

public class CaseE {
  private String id;

  public static void main(String[]argv) {
    CaseE cea = new CaseE("a");
    CaseE ceb = new CaseE("b");
    ((I)cea).methodOne();
    ((I)ceb).methodTwo();
    ((I)cea).methodOne();
    ((I)ceb).methodTwo();
  }

  public CaseE(String id) {
    this.id=id;
  }

  public String toString() {
    return "CaseE instance: "+id;
  }
}

aspect X {
  @DeclareMixin("CaseE")
  public I createImplementation(Object o) {
    System.out.println("Delegate factory invoked for "+o.toString());
    return new Implementation(o);
  }
}

interface I {
  void methodOne();
  void methodTwo();
}

class Implementation implements I {
  Object o;

  public Implementation(Object o) {
    this.o = o;
  }

  public void methodOne() {
    System.out.println("methodOne running on "+o);
  }
  public void methodTwo() {
    System.out.println("methodTwo running on "+o);
  }
}
