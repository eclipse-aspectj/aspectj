package a;

@TypeAnnotation
public aspect AnnotatedAspect08 {
	
	// should just compile harmlessly
	
	@AnyAnnotation
	declare warning : get(* *) : "bah humbug";
	
	@FieldAnnotation
	declare error: set(* *) : "hum bahbug";
	
	@AnyAnnotation
	declare soft : execution(* *) : Exception;
	
	@ConstructorAnnotation
	declare parents : A implements I;
	
	@AnyAnnotation
	declare parents : A extends B;
}

class A {}
class B {}
interface I {}

