import java.lang.reflect.*;
import java.lang.annotation.*;

public class A {
  public static void main(String []argv) throws Exception {
    Method m = A.class.getDeclaredMethod("foo");
    printM(m);
  }
  
  private static void printM(Method m) {
	  System.out.println(m.getName());
	  Annotation[] as = m.getAnnotations();
	  for (Annotation a: as) {
		  System.out.println(a);
	  }
  }
}

aspect X {
  public void A.foo() {}
}
