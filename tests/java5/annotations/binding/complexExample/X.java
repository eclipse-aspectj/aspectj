// Color should be resolved via the import statement...
import d.e.f.Color;

public aspect X {

  before(): call(* *(..)) && @annotation(Color) {
    System.err.println("Before call to "+thisJoinPoint);
  }
  
  before(): execution(* *(..)) && @annotation(Color) {
    System.err.println("Before execution of "+thisJoinPoint);
  }
}
