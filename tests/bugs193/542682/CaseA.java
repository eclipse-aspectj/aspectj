import java.lang.annotation.*;
import org.aspectj.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface SomeAnnotation {}

@SomeAnnotation
public class CaseA {
  public static void main(String[]argv) {
    CaseA ca = new CaseA();
    ((I)ca).methodOne(); // will only succeed if mixin applied
  }
}

@SomeAnnotation
enum Color {R,G,B}

aspect X {
  @DeclareMixin("(@SomeAnnotation *)")
  public static I createImplementation() {
    System.out.println("Delegate factory invoked");
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

