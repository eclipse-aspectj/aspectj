package a;

@TypeAnnotation
public aspect AnnotatedAspect06 {
	
	@ConstructorAnnotation
	before() : execution(* *(..)) {}
	
	@MethodAnnotation
	after() returning : set(* *) {}
	
	@AnyAnnotation
	after() throwing : get(* *) {}
	
	@MethodAnnotation
	after() : handler(*) {}
	
	@MethodAnnotation
	void around() : call(new(..)) { proceed(); }
	
}

