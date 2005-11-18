import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

@Target({ElementType.FIELD})
@interface FieldAnnotation{}

public class AndTypePattern {

	public void method1() {}
	
	@FieldAnnotation
	int field = 1;

}

aspect A {
	
	// should display an xlint message because @FieldAnnotation can't be
	// applied to methods
	pointcut andPointcut() : execution(@(FieldAnnotation && MethodAnnotation) * *(..));
	declare warning : andPointcut() : "andPointcut()";

}
