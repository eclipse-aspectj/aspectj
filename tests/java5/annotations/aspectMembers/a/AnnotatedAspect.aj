package a;

@TypeAnnotation
public aspect AnnotatedAspect {
	
	@FieldAnnotation int foo = 5;
	
	@MethodAnnotation int getFoo() { return foo; }
	
	@ConstructorAnnotation
	public AnnotatedAspect() {}
	
}

aspect VerifyAnnotations {
	
	declare warning : set(@FieldAnnotation * *) : "annotated field";
	declare warning : execution(@MethodAnnotation * *(..)) : "annotated method";
	declare warning : execution(@ConstructorAnnotation new(..)) : "annotated constructor";
	declare warning : staticinitialization(@TypeAnnotation *) : "annotated type";
	
	
}