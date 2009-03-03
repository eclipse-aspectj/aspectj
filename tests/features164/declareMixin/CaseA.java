import org.aspectj.lang.annotation.*;

public class CaseA {
  public static void main(String[]argv) {
    CaseA ca = new CaseA();
    ((I)ca).methodOne();
  }
}

aspect X {
  @DeclareMixin("CaseA")
  public I createImplementation() {
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
