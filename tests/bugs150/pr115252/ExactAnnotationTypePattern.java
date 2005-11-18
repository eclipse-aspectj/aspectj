import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
@interface TypeAnnotation{}

@Target({ElementType.METHOD})
@interface MethodAnnotation{}

@Target({ElementType.FIELD})
@interface FieldAnnotation{}

@interface AnyAnnotation{}

public class ExactAnnotationTypePattern {

	public void method1() {}
	
	@FieldAnnotation
	int field = 1;

}

aspect A {
	
	// an xlint message should be displayed because @TypeAnnotation can only
	// be applied to types, not methods
	pointcut typePC() : execution(@TypeAnnotation * ExactAnnotationTypePattern.method1(..));
	declare warning : typePC() : "blah";
	
	// should compile as normal, since @MethodAnnotation can be applied to methods
	pointcut methodPC() : execution(@MethodAnnotation * ExactAnnotationTypePattern.method1(..));
	declare warning : methodPC() : "blah";
	
	// an xlint message should be displayed because @FieldAnnotation can only
	// be applied to fields, not methods
	pointcut matchAll() : execution(@FieldAnnotation * *(..));
	declare warning : matchAll() : "blah";

	// should compile as normal since @FieldAnnotation can be applied to fields
	pointcut legalFieldPC() : set(@FieldAnnotation int ExactAnnotationTypePattern.field);
	declare warning : legalFieldPC() : "field blah";
	
	// an xlint message should be displayed because @MethodAnnotation can
	// only be applied to methods, not fields
	pointcut illegalFieldPC() : set(@MethodAnnotation int *);
	declare warning : illegalFieldPC() : "field blah blah";
	
	// no xlint message should be displayed here because @AnyAnnotation
	// has the default target
	pointcut anyAnnotation() : execution(@AnyAnnotation * *(..));
	declare warning : anyAnnotation() : "default target is allowed everywhere";

}
