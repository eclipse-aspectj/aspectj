import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


@Retention(RetentionPolicy.RUNTIME) @interface One {}
@Retention(RetentionPolicy.RUNTIME) @interface Two {}
@Retention(RetentionPolicy.RUNTIME) @interface Three {}
@Retention(RetentionPolicy.RUNTIME) @interface Four {}
@Retention(RetentionPolicy.RUNTIME) @interface Five {}
@Retention(RetentionPolicy.RUNTIME) @interface Six {}

interface Target {	
  public void m();
}

class A implements Target {
  public void m() {
	  
  }
}

aspect X {
  declare @method: * Target+.*(..): @One;
  declare @method: * Target+.*(..): @Two;
}

public class DecA2 {
  public static void main(String []argv) {
	  try {
		  Class c = Target.class;
		  Method m = c.getDeclaredMethod("m",null);
		  System.err.println("There are "+m.getAnnotations().length+" annotations on public void Target.m():");
		  dumpAnnos(m.getAnnotations());  
		  c = A.class;
		  m = c.getDeclaredMethod("m",null);
		  System.err.println("There are "+m.getAnnotations().length+" annotations on public void A.m():");
		  dumpAnnos(m.getAnnotations());  
	  } catch (Exception e) { e.printStackTrace();}
  }
  
  public static void dumpAnnos(Annotation[] anns) {
	  List l = new ArrayList();
	  if (anns!=null) {
		  for (int i = 0; i < anns.length; i++) {
			  l.add(anns[i].annotationType().getName());
		  }
	  }
	  Collections.sort(l);
	  int i = 1;
	  for (Iterator iter = l.iterator(); iter.hasNext();) {
		System.err.println((i++)+") "+iter.next());
	  }
  }
}