// Annotated ITD (field) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}

public aspect BindingWithAnnotatedItds2 {

  @Fruit("banana") int A.i;
 
  public @Fruit("apple") String A.j;

  private @Fruit("orange") int[] A.k;
  
  public static void main(String[]argv) {
    A a = new A();
    a.i = 5;
    a.j = "hello";
    a.k = new int[]{1,2,3};
  }
  
}

class A { }

aspect X {

  before(Fruit f): set(* *) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }

  //before(Fruit f): execution(* *(..)) && @annotation(f) {
  //  System.err.println("Execution of something fruity at this jp"+thisJoinPoint+
  //    " ("+thisJoinPoint.getSourceLocation()+")");
  //}
  
}
