import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
@interface TypeAnnotation{}

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

public class AnnotationParameterType {

	public void method1(MyClass m) {
	}
	
}

@TypeAnnotation
class MyClass {
	
}

aspect A {
	
	// shouldn't get an xlint warning because looking method which
	// takes an argument that has the @TypeAnnotation
	pointcut pc() : execution(* *(@TypeAnnotation *));
	declare warning : pc() : "* *(@TypeAnnotation *)";
	
	// should get an xlint warning because can only have the default, 
	// or @Target{ElementType.TYPE} as an argument type
	pointcut incorrectArgumentType() : execution(* *(@MethodAnnotation *));
	declare warning : incorrectArgumentType() : "argument type can only have @Target{ElementType.TYPE}";
		
}
