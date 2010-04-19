import java.lang.reflect.*;
import java.lang.annotation.*;

public class B {
  public static void main(String []argv) {
    Field[] fs = B.class.getDeclaredFields();
    for (Field f: fs) {
    	printM(f);
    }
  }
  
  private static void printM(Field m) {
	  System.out.println(m.getName());
	  Annotation[] as = m.getAnnotations();
	  for (Annotation a: as) {
		  System.out.println(a);
	  }
  }
}

aspect X {
  public int B.boo;
}
