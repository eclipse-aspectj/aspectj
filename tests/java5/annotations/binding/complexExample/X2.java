// Same as X but includes annotation binding
import d.e.f.Color;

public aspect X2 {

  before(Color c): call(* *(..)) && @annotation(c) {
    System.err.println("Before call to "+thisJoinPoint+" color is "+c);
  }
  
  before(Color c): execution(* *(..)) && @annotation(c) {
    System.err.println("Before execution of "+thisJoinPoint+" color is "+c);
  }

}
