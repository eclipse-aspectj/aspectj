package a;

@MethodAnnotation
public aspect AnnotatedAspect02 {
	
	@TypeAnnotation int goo;
	
	@FieldAnnotation int getGoo() { return goo; }
	
	@AnnotationAnnotation AnnotatedAspect02() { goo = 5; }
	
}

aspect VerifyAnnotations {
	
	declare warning : set(@FieldAnnotation * *) : "annotated field";
	declare warning : execution(@MethodAnnotation * *(..)) : "annotated method";
	declare warning : execution(@ConstructorAnnotation new(..)) : "annotated constructor";
	declare warning : staticinitialization(@TypeAnnotation *) : "annotated type";
	
	
}