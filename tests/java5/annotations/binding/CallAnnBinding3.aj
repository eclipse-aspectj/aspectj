import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class CallAnnBinding3 {
  public static void main(String[]argv) {
  	SecondaryClass sc = new SecondaryClass();
  	
  	// tackle the primitives ...
  	
  	sc.m1(1);                        // int
  	sc.m2(true);                     // boolean
  	sc.m3(new Byte("3").byteValue());// byte
  	sc.m4('a');                      // char
  	sc.m5(new Long(444L).longValue());      // long
  	sc.m6(new Double(3.3).doubleValue());   // double
  	sc.m7(new Float(2.2).floatValue());     // float
  	sc.m8(new Short("2").shortValue());     // short
  	
  	// how about darn arrays
  	
  	sc.m9(new int[]{1,2,3},new String[]{"a","b"});
  	
  	// and the ultimate ... double depth dastardly arrays
  	
  	String a[][] = new String[5][];
  	a[0]= new String[3];
  	sc.m10(a);
  }

}

class SecondaryClass {
	
	@Colored(color="red")    public void m1(int i) {	}
	@Colored(color="orange") public void m2(boolean b) {}	
	@Colored(color="yellow") public void m3(byte b) { }
	@Colored(color="green")  public void m4(char c) { }
	@Colored(color="blue")   public void m5(long l) { }
	@Colored(color="indigo") public void m6(double d) { }
	@Colored(color="violet") public void m7(float f) { }
	@Colored(color="black")  public void m8(short s) { }
	
	@Colored(color="taupe")  public void m9(int[] x,String[] ss) {}
	
	@Colored(color="beige")  public void m10(String[][] s) {}
}

aspect X {
  int i = 0;
  
  before(Colored c): call(* SecondaryClass.*(..)) && !within(X) && @annotation(c) {
  	i++;
  	if (i==1) checkColor(1,c,"red");
  	if (i==2) checkColor(2,c,"orange");
  	if (i==3) checkColor(3,c,"yellow");
  	if (i==4) checkColor(4,c,"green");
  	if (i==5) checkColor(5,c,"blue");
  	if (i==6) checkColor(6,c,"indigo");
  	if (i==7) checkColor(6,c,"violet");
  	if (i==8) checkColor(6,c,"black");
  	
  	if (i==9) checkColor(6,c,"taupe");

  	if (i==10) checkColor(6,c,"beige");
  	
  	if (i==11) throw new RuntimeException("Advice running more times than expected");
  	System.err.println(c.color());
  }
  
  public void checkColor(int run, Colored c,String exp) {
  	if (!c.color().equals(exp)) 
  		throw new RuntimeException("Advice execution #"+run+" expected "+exp+" but got "+c.color());
  }
}

