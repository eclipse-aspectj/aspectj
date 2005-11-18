import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
@interface TypeAnnotation{}

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

@TypeAnnotation
public class AnnotationDeclaringType {

	public void method1() {
	}
	
}

aspect A {
	
	// matches the execution of any method where the declaring type
	// has the @TypeAnnotation - should compile ok and get no xlint errors
	pointcut pc() : execution(* (@TypeAnnotation *).*(..));
	declare warning : pc() : "* (@TypeAnnotation *).*(..)";
	
	// should get an xlint warning because declaring types can only
	// have the default @Target or @Target{ElementType.TYPE} target
	pointcut pc2() : execution(* (@MethodAnnotation *).*(..));
	declare warning : pc2() : "* (@MethodAnnotation *).*(..)";
}
