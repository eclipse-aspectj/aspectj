import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class CtorAnnBinding2 {
  public static void main(String[]argv) {
    new C();
    new C("hello");
    new C(new int[]{1,2,3});
    X.verifyRun();
  }

  static class C {

    @Colored(color="red")   public C() {System.err.println("<init>() running");}
    @Colored(color="green") public C(String s) {System.err.println("<init>("+s+") running");}
    @Colored(color="blue")  public C(int[] is) {System.err.println("<init>(int[]) running");}
    public C(boolean b) {System.err.println("<init>("+b+") running");}
  }

}

aspect X {
	 
	  // Expected color order
	  static String exp[] = new String[]{"red","green","blue"};
	  
	  static int i = 0; // Count of advice executions
	  
	  before(Colored c): execution(new(..)) && within(CtorAnnBinding2) && @annotation(c) {
	    System.err.println(thisJoinPoint+" color="+c.color());
	  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
	  	i++;
	  }
	  
	  public static void verifyRun() {
	  	if (X.i != exp.length)
	  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
	  }
}