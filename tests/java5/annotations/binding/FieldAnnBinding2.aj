import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color();}

public class FieldAnnBinding2 {

  @Colored(color="red") static int i;

  @Colored(color="blue") String s;

  @Colored(color="green") boolean b;

  public static void main(String[]argv) {
    FieldAnnBinding2 fab = new FieldAnnBinding2();
    i = 5;
    fab.s = "abc";
    fab.b = true;

    System.err.println("i="+fab.i);
    System.err.println("s="+fab.s);
    System.err.println("b="+fab.b);
    X.verifyRun();
  }
}
aspect X {
	 
	  // Expected color order
	  static String exp[] = new String[]{"red","blue","green"};
	  
	  static int i = 0; // Count of advice executions
	  
	  before(Colored c): get(* *) && withincode(* main(..)) && @annotation(c) {
	    System.err.println(thisJoinPoint+" color="+c.color());
	  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
	  	i++;
	  }
	  
	  public static void verifyRun() {
	  	if (X.i != exp.length)
	  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
	  }
}
