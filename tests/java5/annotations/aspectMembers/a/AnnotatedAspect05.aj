package a;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import java.lang.annotation.Annotation;

@TypeAnnotation
public aspect AnnotatedAspect05 {
	
//	@ConstructorAnnotation 
//	before() : execution(* *(..)) {}
	
	@MethodAnnotation
	@SuppressAjWarnings
	after() returning : set(* *) {}
	
	@AnyAnnotation
	after() throwing : get(* *) {}
	
	@MethodAnnotation
	@SuppressAjWarnings
	after() : handler(*) {}
	
	@MethodAnnotation
	@SuppressAjWarnings("adviceDidNotMatch")
	Object around() : call(new(..)) { return proceed(); }
	
	public static void main(String[] args) {
  		java.lang.reflect.Method[] methods = AnnotatedAspect05.class.getDeclaredMethods();
  		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("ajc$afterThrowing")) {
				Annotation annotation = methods[i].getAnnotation(AnyAnnotation.class);
				if (annotation == null) {
					throw new RuntimeException("advice should be annotated");
				}
			}
		}
	}
}

