import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

interface Marker { @Colored(color="blue") public void m1(); public void m2();  }

public class CallAnnBinding4 {
  public static void main(String[]argv) {
  	Marker marker = new SecondaryClass();
  	
  	// tackle the primitives ...
  	
  	marker.m1();
  	marker.m2();
  	
  	if (X.i!=1) throw new RuntimeException("Why did the advice not run once?");
  }

}

class SecondaryClass implements Marker {
	
	@Colored(color="red")    public void m1() {}
	@Colored(color="orange") public void m2() {}	
}

aspect X {
  public static int i = 0;
  
  before(Colored c): call(* m*(..)) && !within(X) && @annotation(c) {
  	i++;
  	if (i==1) checkColor(1,c,"blue");
  	
  	if (i==2) throw new RuntimeException("Advice running more times than expected");
  	System.err.println(c.color());
  }
  
  public void checkColor(int run, Colored c,String exp) {
  	if (!c.color().equals(exp)) 
  		throw new RuntimeException("Advice execution #"+run+" expected "+exp+" but got "+c.color());
  }
}

