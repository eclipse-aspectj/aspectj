package pkg;

aspect A {

	declare @type : C : @MyAnnotation;
	declare @field : int C.someField : @MyAnnotation;
	declare @method : public void C.method1() : @MyAnnotation;
	declare @constructor : C.new() : @MyAnnotation;
	
}

class C {
	
	public C() {
	}
	
	int someField = 3;
	
	public void method1() {
	}
	
}

@interface MyAnnotation{}
