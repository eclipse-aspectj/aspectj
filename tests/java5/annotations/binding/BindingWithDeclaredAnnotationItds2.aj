// Annotated ITD (field) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}

public aspect BindingWithDeclaredAnnotationItds2 {

  int A.i;

  private String A.j;

  public boolean[] A.k;
  
  public static void main(String[]argv) {
    A a = new A();
    a.i = 5;
    a.j = "hello";
    a.k = new boolean[]{true,false};
  }
  
}

class A { }

aspect X {

  declare @field: int A.i: @Fruit("orange");

  declare @field: String A.j: @Fruit("banana");

  declare @field: boolean[] A.k: @Fruit("apple");

  before(Fruit f): set(* *) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }

  
}
