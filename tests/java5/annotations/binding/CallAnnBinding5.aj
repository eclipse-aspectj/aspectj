import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

interface Marker { public void m1(); public void m2();  }

public class CallAnnBinding5 {
  public static void main(String[]argv) {
  	SecondaryClass sc = new SecondaryClass();
  	sc.m1();
  	sc.m2();
  	if (X.i!=1) throw new RuntimeException("Why did the advice not run?");
  }

}

class SecondaryClass {
	@Colored(color="red")  public void m1() {	}
	public void m2() {}	
}

aspect X {
  public static int i = 0;
  
  before(Colored c): call(* SecondaryClass.*(..)) && !within(X) && @annotation(c) {
  	i++;
  	if (i==1) checkColor(1,c,"red");
  	
  	if (i==11) throw new RuntimeException("Advice running more times than expected");
  	System.err.println(c.color());
  }
  
  public void checkColor(int run, Colored c,String exp) {
  	if (!c.color().equals(exp)) 
  		throw new RuntimeException("Advice execution #"+run+" expected "+exp+" but got "+c.color());
  }
}

