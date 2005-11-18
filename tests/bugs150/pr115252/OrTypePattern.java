import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
@interface TypeAnnotation{}

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

@Target({ElementType.FIELD})
@interface FieldAnnotation{}

public class OrTypePattern {

	public void method1() {}
	
	@FieldAnnotation
	int field = 1;

}

aspect A {
	
	// should display an xlint message because @FieldAnnotation can't be
	// applied to methods
	pointcut orPointcut() : execution(@(FieldAnnotation || MethodAnnotation) * *(..));
	declare warning : orPointcut() : "orPointcut()";
	
	// two xlint messages should be displayed because neither @FieldAnnotation
	// or @TypeAnnotation can match methods
	pointcut orPointcut2() : execution(@(FieldAnnotation || TypeAnnotation) * *(..));
	declare warning : orPointcut2() : "orPointcut2()";

}
