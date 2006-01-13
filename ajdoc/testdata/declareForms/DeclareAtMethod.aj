package foo;

@interface MyAnnotation {	
}

public aspect DeclareAtMethod {

	declare @method : public * C.*(..) : @MyAnnotation;
	
}

class C {
	
	public void amethod() {
	}
	
}
