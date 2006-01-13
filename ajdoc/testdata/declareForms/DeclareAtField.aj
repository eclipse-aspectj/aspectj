package foo;

@interface MyAnnotation {	
}

public aspect DeclareAtField {

	declare @field : int C.* : @MyAnnotation;
	
}

class C {
	
	int x = 1;
	
}
