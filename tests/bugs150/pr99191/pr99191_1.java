@interface Annotation{}
aspect B {
	
    declare @field : int C.noSuchField : @Annotation; // should be an error
	declare @field : int B.noSuchField : @Annotation; // should be an error
    
}

class C {
}
