// Annotated ITD (method) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}

public aspect BindingWithAnnotatedItds1 {

  @Fruit("apple") int A.m() { return 1; }
  
  public static void main(String[]argv) {
    A a = new A();
    a.m();
  }
  
}

class A { }

aspect X {

  before(Fruit f): execution(* *(..)) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }
  
}
