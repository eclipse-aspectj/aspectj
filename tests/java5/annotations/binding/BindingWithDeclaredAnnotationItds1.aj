// Annotated ITD (method) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}

public aspect BindingWithDeclaredAnnotationItds1 {

  int A.m() { return 1; }

  public int A.n() { return 2; }
   
  private int A.o() { return 3; }
  
  public static void main(String[]argv) {
    A a = new A();
    a.m();
    a.n();
    a.o();
  }
  
}

class A { }

aspect X {

  declare @method: int A.m(): @Fruit("orange");

  declare @method: int A.n(): @Fruit("banana");

  declare @method: int A.o(): @Fruit("tomato");

  before(Fruit f): execution(* *(..)) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }

  before(Fruit f): call(* *(..)) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }

  
}
