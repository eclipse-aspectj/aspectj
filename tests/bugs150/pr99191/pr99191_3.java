@interface Annotation{}
aspect B {
	
	declare @method : public * C.noSuchMethod(..) : @Annotation; // should be an error
	declare @method : * B.noSuchMethod(..) : @Annotation; // should be an error
    
}

class C {
}
