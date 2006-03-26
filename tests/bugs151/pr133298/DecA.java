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

class Target {
	
  public void m() {}
  
  public Target(int i) {}
  
  public int x;
}

aspect X {
  declare @method: * Target.*(..): @One;
  declare @method: * Target.*(..): @Two;
  

  declare @constructor: Target.new(int): @Three;
  declare @constructor: Target.new(int): @Four;
  

  declare @field: int Target.*: @Five;
  declare @field: int Target.*: @Six;
}

public class DecA {
  public static void main(String []argv) {
	  // Let's do the method then the ctor, then the field
	  try {
		  Class c = Target.class;
		  Method m = c.getDeclaredMethod("m",null);
		  System.err.println("There are "+m.getAnnotations().length+" annotations on public void m():");
		  dumpAnnos(m.getAnnotations());  
		  Constructor ctor = c.getDeclaredConstructor(new Class[]{Integer.TYPE});
		  System.err.println("There are "+ctor.getAnnotations().length+" annotations on public Target(int):");
		  dumpAnnos(ctor.getAnnotations());  
		  Field f = c.getDeclaredField("x");
		  System.err.println("There are "+f.getAnnotations().length+" annotations on public int x:");
		  dumpAnnos(f.getAnnotations());  
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