import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class CallAnnBinding2 {
  public static void main(String[]argv) {
  	SecondaryClass sc = new SecondaryClass();
  	sc.m1(1);
  	sc.m2("hello");
  	sc.m3("hello",1);
  }

}

class SecondaryClass {
	
	@Colored(color="cyan")
	public void m1(int i) {
	}
	
	@Colored(color="magenta")
	public void m2(String s) {
	}
	
	@Colored(color="mauve")
	public void m3(String s,int i) {		
	}
}

aspect X {
  int i = 0;
  
  before(Colored c): call(* SecondaryClass.*(..)) && !within(X) && @annotation(c) {
  	i++;
  	if (i==1) checkColor(1,c,"cyan");
  	if (i==2) checkColor(2,c,"magenta");
  	if (i==3) checkColor(3,c,"mauve");
  	System.err.println(c.color());
  }
  
  public void checkColor(int run, Colored c,String exp) {
  	if (!c.color().equals(exp)) 
  		throw new RuntimeException("Advice execution #"+run+" expected "+exp+" but got "+c.color());
  }
}

