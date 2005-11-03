package a;

@TypeAnnotation
public aspect AnnotatedAspect03 {
	
	@FieldAnnotation int foo = 5;
	
	@FieldAnnotation private int ITDMe.goo = 3;
	
	@MethodAnnotation private int ITDMe.getGoo() { return goo; }
	
	@ConstructorAnnotation public ITDMe.new(int x) { this(); goo = x; }
	
}

class ITDMe {}

aspect VerifyAnnotations {
	
	declare warning : set(@FieldAnnotation * *) : "annotated field";
	declare warning : execution(@MethodAnnotation * *(..)) : "annotated method";
	declare warning : execution(@ConstructorAnnotation new(..)) : "annotated constructor";
	declare warning : staticinitialization(@TypeAnnotation *) : "annotated type";
	
	
}