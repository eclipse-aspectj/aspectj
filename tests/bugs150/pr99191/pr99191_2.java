@interface Annotation{}
aspect B {
	declare @field : int C.anotherField : @Annotation; // should be woven
	declare @field : int someField : @Annotation; // shouldn't have any errors
	declare @field : int C.aField : @Annotation; // shouldn't have any errors
}

class C {
	@Annotation int aField = 1;
}

aspect D {
	public int C.anotherField;
}
