import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color();}

public class FieldAnnBinding1 {

  @Colored(color="red") static int i;

  @Colored(color="blue") String s;

  @Colored(color="green") boolean b;

  public static void main(String[]argv) {
    i = 5;
    new FieldAnnBinding1().s = "abc";
    new FieldAnnBinding1().b = true;
    X.verifyRun();
  }
}

aspect X {
	 
	  // Expected color order
	  static String exp[] = new String[]{"red","blue","green"};
	  
	  static int i = 0; // Count of advice executions
	  
	  before(Colored c): set(* *) && withincode(* main(..)) && @annotation(c)  {
	    System.err.println(thisJoinPoint+" color="+c.color());
	  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
	  	i++;
	  }
	  
	  public static void verifyRun() {
	  	if (X.i != exp.length)
	  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
	  }
}
