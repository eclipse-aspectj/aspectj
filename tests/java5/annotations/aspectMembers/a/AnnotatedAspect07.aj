package a;

@TypeAnnotation
public aspect AnnotatedAspect07 {
	
	// should just compile harmlessly
	
	@AnyAnnotation
	pointcut foo() : get(* *);
}

