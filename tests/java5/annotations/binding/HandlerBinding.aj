import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class HandlerBinding {
  public static void main(String[]argv) {
    try {
      throw new AndyException();
    } catch (AndyException ae) {
      System.err.println(ae);
    }
    X.verifyRun();
  }

  @Colored(color="red") static class AndyException extends Exception {
    public AndyException() {
    }
  }

}

aspect X {
	 
	  // Expected color order
	  static String exp[] = new String[]{"red"};
	  
	  static int i = 0; // Count of advice executions
	  
	  before(Colored c): handler(*) && !within(X) && @annotation(c) {
	    System.err.println(thisJoinPoint+" color="+c.color());
	  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
	  	i++;
	  }
	  
	  public static void verifyRun() {
	  	if (X.i != exp.length)
	  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
	  }
	}
