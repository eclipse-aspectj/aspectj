import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color();}

public class WithinCodeBinding1 {

  @Colored(color="red") void mRed() {System.out.println("red"); }

  @Colored(color="blue") void mBlue() {System.out.println("blue");}

  @Colored(color="green") void mGreen() {System.out.println("green");}

  @Colored(color="yellow") WithinCodeBinding1() {
  	System.out.println("yellow");
  }

  public static void main(String[]argv) {
    WithinCodeBinding1 instance = new WithinCodeBinding1();
    instance.mRed();
    instance.mBlue();
    instance.mGreen();
    X.verifyRun();
  }
}

aspect X {

    // Expected color order
    static String exp[] = new String[]{"yellow","red","blue","green"};
    
    static int i = 0; // Count of advice executions
    
    before(Colored c): @withincode(c) && call(* println(..)) {
      System.err.println(thisJoinPoint+" color="+c.color());
    	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
    	i++;
    }
    
    public static void verifyRun() {
    	if (X.i != exp.length)
    		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
    }
}
