import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color();}

public class FieldAnnBinding3 {

  @Colored(color="red")  String s[];

  @Colored(color="blue") boolean b[];

  public static void main(String[]argv) {
    FieldAnnBinding3 fab = new FieldAnnBinding3();
    fab.s = new String[]{"abc","def"};
    fab.b = new boolean[]{true,false,true};

    System.err.println("s="+fab.s);
    System.err.println("b="+fab.b);
    X.verifyRun();
  }
}
aspect X {
	 
	  // Expected color order
	  static String exp[] = new String[]{"red","blue"};
	  
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