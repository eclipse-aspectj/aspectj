import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class AdviceExecBinding {
  public static void main(String[]argv) {
    m1();
    m2();
    m3();
    X.verifyRun();
  }

  static void m1() {}
  static void m2() {}
  static void m3() {}
}

aspect B {
  @Colored(color="orange") before(): execution(* m1()) {}
  @Colored(color="yellow") before(): execution(* m2()) {}
  @Colored(color="brown")  before(): execution(* m3()) {}
}

aspect X {
 
  // Expected color order
  static String exp[] = new String[]{"orange","yellow","brown"};
  
  static int i = 0; // Count of advice executions
  
  before(Colored c): adviceexecution() && within(B) && @annotation(c) {
    System.err.println(thisJoinPoint+" color="+c.color());
  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
  	i++;
  }
  
  public static void verifyRun() {
  	if (X.i != exp.length)
  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
  }
}

