import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class StaticInitBinding {
  public static void main(String[]argv) {
    new A();
    new B();
    new C();
    X.verifyRun();
  }
}

@Colored(color="orange") class A {}
@Colored(color="yellow") class B {}
@Colored(color="brown") class C {}


aspect X {
 
  // Expected color order
  static String exp[] = new String[]{"orange","yellow","brown"};
  
  static int i = 0; // Count of advice executions
  
  before(Colored c): staticinitialization(*) && !within(X) && @annotation(c) {
    System.err.println(thisJoinPoint+" color="+c.color());
  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
  	i++;
  }
  
  public static void verifyRun() {
  	if (X.i != exp.length)
  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
  }
}