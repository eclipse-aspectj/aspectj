// Annotated ITD (field) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}
@Retention(RetentionPolicy.RUNTIME) @interface Drink { String value();}

public aspect BindingWithDeclaredAnnotationItds3 {

  int A.i;

  public static void main(String[]argv) {
    A a = new A();
    a.i = 5;
  }
  
}

class A { }

aspect X {

  declare @field: int A.i: @Fruit("orange");
  declare @field: int A.i: @Drink("margarita");

  before(Fruit f): set(* *) && @annotation(f) {
    System.err.println("Found fruit "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }

  before(Drink d): set(* *) && @annotation(d) {
    System.err.println("Found drink "+d.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }

  
}
