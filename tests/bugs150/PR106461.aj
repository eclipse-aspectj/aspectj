public aspect PR106461 {
	
	pointcut missingNamePattern() : call(* Foo+(..));
	
	pointcut missingNamePatternInField() : get(* Foo+);
	
	pointcut missingNamePatternInConstructor() : call(Foo+(..));
	
	pointcut butThisIsAllowedOfCourse() : call(* *(..));
	
	pointcut asIsThis() : call(* foo(..));
}

class Foo {
	
	void foo() {}
	
}