import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Iterator;

@Retention(RetentionPolicy.RUNTIME)
@interface One {}

@Retention(RetentionPolicy.RUNTIME)
@interface Two {}

class Target {
  public void m() {}
}

aspect X {
  declare @method: * Target.*(..): @One;
  declare @method: * Target.*(..): @Two;
}

public class DecA {
  public static void main(String []argv) {
	  try {
		  Class c = Target.class;
		  Method m = c.getDeclaredMethod("m",null);
		  Annotation[] anns =  m.getAnnotations();
		  System.err.println("There are "+anns.length+" annotations on public void m():");
		  for (int i = 0; i < anns.length; i++) {
			System.err.println((i+1)+") "+anns[i]);
		  }
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
}