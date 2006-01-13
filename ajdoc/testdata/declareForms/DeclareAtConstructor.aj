package foo;

@interface MyAnnotation {	
}

public aspect DeclareAtConstructor {

	declare @constructor : C.new(..) : @MyAnnotation;
	
}

class C {
	
	public C(String s) {		
	}
	
}
