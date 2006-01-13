package foo;

@interface MyAnnotation {	
}

public aspect AnnotationTest {

	declare @type : C : @MyAnnotation;
	
}

class C {
	
}
