import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color();}

@Colored(color="red") class RedClass {
  public void m() { System.err.println("RedClass.m() running"); }
}

@Colored(color="green") class GreenClass {
  public GreenClass() { System.err.println("GreenClass.ctor() running"); }
  public void m() { System.err.println("GreenClass.m() running"); }
}

class NormalClass {
  public void m() { System.err.println("NormalClass.m() running"); }
}


public class WithinBinding2 {
  public static void main(String[]argv) {
    new RedClass().m();
    new NormalClass().m();
    new GreenClass().m();
    X.verifyRun();
  }
}

aspect X {

    static int red = 0;
    static int green=0;
    
    before(Colored c): @within(c) {
        System.err.println(thisJoinPoint+" color="+c.color());
    	if (c.color().startsWith("r")) red++;
    	else if (c.color().startsWith("g")) green++;
    	else throw new RuntimeException("Didn't expect this color: "+c.color());
    }
    
    public static void verifyRun() {
    	System.err.println(red+"  "+green);
    	if (red!=7) throw new RuntimeException("Expected 7 red join points but got "+red);
    	if (green!=9) throw new RuntimeException("Expected 9 green join points but got "+green);
    }
}
