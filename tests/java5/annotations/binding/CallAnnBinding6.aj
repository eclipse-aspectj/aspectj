import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class CallAnnBinding6 {
  public static void main(String[]argv) {
  	A b = new B();
  	b.m();
  	if (X.i!=1) throw new RuntimeException("Why did the advice not run?");
  }
}

class A {
	@Colored(color="RedA") public void m() {}
}
class B extends A {
	@Colored(color="RedB") public void m() {}
}

aspect X {
  public static int i = 0;
  
  before(Colored c): call(* m*(..)) && !within(X) && @annotation(c) {
  	i++;
  	if (i==1) checkColor(1,c,"RedA");
  	
  	if (i==11) throw new RuntimeException("Advice running more times than expected");
  	System.err.println(c.color());
  }
  
  public void checkColor(int run, Colored c,String exp) {
  	if (!c.color().equals(exp)) 
  		throw new RuntimeException("Advice execution #"+run+" expected "+exp+" but got "+c.color());
  }
}

