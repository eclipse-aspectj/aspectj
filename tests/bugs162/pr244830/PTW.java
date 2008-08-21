import java.lang.reflect.*;

aspect X pertypewithin(A*) {
  before(): execution(* *(..)) {}
}

class A {
  public void foo() {}
}

class AA {
  public void foo() {}
}
public class PTW {
  public void foo() {}
  
  public static void main(String []argv) {
	  Field[] fs = X.class.getDeclaredFields();
	  for (int i=0;i<fs.length;i++) {
		  if (fs[i].getName().equals("ajc$initFailureCause")) {
			  throw new RuntimeException("Should be no ajc$initFailureCause field for ptw");
		  }
	  }
  }
}

