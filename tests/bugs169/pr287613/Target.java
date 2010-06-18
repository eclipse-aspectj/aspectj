import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Annot1 {}
@Retention(RetentionPolicy.RUNTIME)
@interface Annot2 {}
@Retention(RetentionPolicy.RUNTIME)
@interface Annot3 {}

public class Target {
  public static void main(String[] argv) throws Exception {
	  System.out.println("Field one");
	  printAnnotations(A.class.getDeclaredField("one"));
	  printAnnotations(B.class.getDeclaredField("one"));
	  System.out.println("Field two");
	  printAnnotations(A.class.getDeclaredField("two"));
	  printAnnotations(B.class.getDeclaredField("two"));
	  System.out.println("Field three");
	  printAnnotations(A.class.getDeclaredField("three"));
	  printAnnotations(B.class.getDeclaredField("three"));
  }
  
  public static void printAnnotations(Field field) {
	  Annotation[] annos = field.getAnnotations();
	  if (annos==null || annos.length==0) {
		  System.out.println("no annotations");
	  } else {
		  for (Annotation anno: annos) {
			  System.out.print(anno+" ");
		  }
		  System.out.println();
	  }
  }
}

class A {
	public int one;
	public String two;
	public float three;
}

class B {
	public int one;
	public String two;
	public float three;
}

aspect DeclareAnnot {
	declare @field: (int A.one) || (int B.one): @Annot1;
	declare @field: (String two) && !(* B.*): @Annot2;
	declare @field: !(* one) && !(* two): @Annot3;
}
