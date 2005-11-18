import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.METHOD, ElementType.FIELD})
@interface MethodAndFieldAnnotation{}

@Target({ElementType.TYPE, ElementType.METHOD})
@interface TypeAndMethodAnnotation{}


public class MoreThanOneTargetAnnotation {

	public void method1() {}
	
	int field = 1;

}

aspect A {
	
	// shouldn't get an xlint message because @MethodAndFieldAnnotation
	// can be applied to methods and fields
	pointcut pc1() : execution(@MethodAndFieldAnnotation * *(..));
	declare warning : pc1() : "pc1()";
	
	// should get an xlint message because can only have the target
	// ElementType.TYPE as a return type
	pointcut pc2() : execution((@MethodAndFieldAnnotation *) *(..));
	declare warning : pc2() : "pc2()";

	// shouldn't get an xlint message because can have the target
	// ElementType.TYPE as a return type
	pointcut pc3() : execution((@TypeAndMethodAnnotation *) *(..));
	declare warning : pc3() : "pc3()";

	// should get an xlint message because @TypeAndMethodAnnotation
	// can only be applied to types and methods, not fields
	pointcut pc4() : set(@TypeAndMethodAnnotation int *);
	declare warning : pc4() : "pc4()";
		
}
