package foo;

@interface MyAnnotation {	
}

public aspect DeclareAtType {

	declare @type : C : @MyAnnotation;
	
}

class C {
	
}
