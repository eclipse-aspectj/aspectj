package a;

import org.aspectj.lang.annotation.SuppressAjWarnings;

@TypeAnnotation
public aspect AnnotatedAspect05 {
	
//	@ConstructorAnnotation 
//	before() : execution(* *(..)) {}
	
	@MethodAnnotation
	@SuppressAjWarnings
	after() returning : set(* *) {}
	
	@AnyAnnotation
	after() throwing : get(* *) {}
	
	@MethodAnnotation
	@SuppressAjWarnings
	after() : handler(*) {}
	
	@MethodAnnotation
	@SuppressAjWarnings
	void around() : call(new(..)) { proceed(); }
	
}

