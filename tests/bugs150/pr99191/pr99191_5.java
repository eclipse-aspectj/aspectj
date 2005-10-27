@interface Annotation{}
aspect B {
	
	declare @constructor : C.new(String) : @Annotation; // should be an error
	declare @constructor : B.new(int) : @Annotation; // should be an error
    
}

class C {
}
