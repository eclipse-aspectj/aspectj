import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
@interface TypeAnnotation{}

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

public class AnnotationThrowsPattern {

	public void method1() throws MyException {}
	
	public void method2() throws MyNonAnnotatedException {}
	
}

@TypeAnnotation
class MyException extends Exception {
	
}

class MyNonAnnotatedException extends Exception {
	
}

aspect A {

	// shouldn't get xlint warnings because @TypeAnnotation is allowed
	pointcut required() : execution(* *.*(..) throws (@TypeAnnotation *));
	declare warning : required() : "(* *.*(..) throws (@TypeAnnotation *))";

	// shouldn't get xlint warnings because @TypeAnnotation is allowed
	pointcut forbidden() : execution(* *.*(..) throws !(@TypeAnnotation *));
	declare warning : forbidden() : "(* *.*(..) throws !(@TypeAnnotation *))";
	
	// should get an xlint warning here because can only have
	// annotations with @Target{ElementType.TYPE} or the default
	// @Target (which is everything)
	pointcut required1() : execution(* *.*(..) throws (@MethodAnnotation *));
	declare warning : required1() : "* *.*(..) throws (@MethodAnnotation *)";

	// should get an xlint warning here because can only have
	// annotations with @Target{ElementType.TYPE} or the default
	// @Target (which is everything)
	pointcut forbidden1() : execution(* *.*(..) throws !(@MethodAnnotation *));
	declare warning : forbidden1() : "* *.*(..) throws !(@MethodAnnotation *)";

}
