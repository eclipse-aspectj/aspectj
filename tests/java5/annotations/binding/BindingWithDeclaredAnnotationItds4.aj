// Annotated ITD (ctor) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}

public aspect BindingWithDeclaredAnnotationItds4 {

  A.new(String s) { }

  private A.new(int i) { }

  public A.new(boolean b) { }
  
  public static void main(String[]argv) {
    A instance1 = new A("a");
    A instance2 = new A(3);
    A instance3 = new A(true);
  }
  
}

class A { 

}

aspect X {

  declare @constructor: A.new(String): @Fruit("pear");
  declare @constructor: A.new(int): @Fruit("orange");
  declare @constructor: A.new(boolean): @Fruit("tomato");

  before(Fruit f): execution(new(..)) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }
  
}
