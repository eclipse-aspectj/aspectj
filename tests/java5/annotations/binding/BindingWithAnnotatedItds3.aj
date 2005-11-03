// Annotated ITD (ctor) being matched upon and extracted
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();}

public aspect BindingWithAnnotatedItds3 {

  @Fruit("pear") A.new(String s) { this();  }

  private @Fruit("orange") A.new(int i) {  this(); }

  public @Fruit("tomato") A.new(boolean b) {  this(); }
  
  public static void main(String[]argv) {
    A instance1 = new A("a");
    A instance2 = new A(3);
    A instance3 = new A(true);
  }
  
}

class A { 

}

aspect X {

  before(Fruit f): execution(new(..)) && @annotation(f) {
    System.err.println("Found "+f.value()+" at jp "+thisJoinPoint+
                       " ("+thisJoinPoint.getSourceLocation()+")");
  }
  
}
