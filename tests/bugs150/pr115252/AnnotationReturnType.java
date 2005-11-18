import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
@interface TypeAnnotation{}

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

public class AnnotationReturnType {

	public MyClass method1() {
		return new MyClass();
	}
	
}

@TypeAnnotation
class MyClass {
	
}

aspect A {
	
	// shouldn't get an xlint warning because looking for a return type
	// which has the @TypeAnnotation annotation
	pointcut pc() : execution((@TypeAnnotation *) *(..));
	declare warning : pc() : "(@TypeAnnotation *) *(..)";
	
	// should get an xlint warning because can only have the default, 
	// or @Target{ElementType.TYPE} as a return type
	pointcut incorrectReturnType() : execution((@MethodAnnotation *) *(..));
	declare warning : incorrectReturnType() : "return type can only have @Target{ElementType.TYPE}";
	
	// should get an xlint warning because @MethodAnnotation can never match
	// but also get a declare warning because the @TypeAnnotation matches
	pointcut orPointcut() : execution((@(TypeAnnotation || MethodAnnotation) *) *(..));
	declare warning : orPointcut() : "(@(TypeAnnotation || MethodAnnotation) *) *(..)";
	
}
