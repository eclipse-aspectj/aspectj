import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color();}

@Colored(color="red")
public class WithinBinding1 {

  void mRed() {System.out.println("red"); }

  void mBlue() {System.out.println("blue");}

  void mGreen() {System.out.println("green");}

  WithinBinding1() {
  	System.out.println("yellow");
  }

  public static void main(String[]argv) {
    WithinBinding1 instance = new WithinBinding1();
    instance.mRed();
    instance.mBlue();
    instance.mGreen();
    X.verifyRun();
  }
}

aspect X {

	static int maxruns = 21; // there are 21 join points in a run of WithinBinding1.main()
    
    static int i = 0; // Count of advice executions
    
    before(Colored c): @within(c) {
      System.err.println(thisJoinPoint+" color="+c.color());
    	if (!c.color().equals("red")) throw new RuntimeException("not red? "+c.color());
    	i++;
    }
    
    public static void verifyRun() {
    	if (X.i != maxruns)
    		throw new RuntimeException("Expected "+maxruns+" advice runs but did "+X.i);
    }
}
