interface A {
public void a(String s);
}

@Annotation
interface B extends A{}

class C implements B {
   public void a(final String s) {}
}

aspect Aspect{
   pointcut foo(): call(* (@Annotation *)+.*(..));
   declare warning : foo() : "matched"; 
   before() : foo() {
	   System.out.println("In advice");
   }
}

public class AnnotationPlusPatternMatchingError {
	
	public static void main(String[] args) {
		new AnnotationPlusPatternMatchingError().testLtw();
	}
	
	public void testLtw() {
		 B anA = new C();
		 anA.a("hi");
	}
	
}

@interface Annotation {}