@interface Annotation{}
aspect B {
	declare @method : public void C.anotherMethod(..) : @Annotation; // shouldn't have any errors
	declare @method : * someMethod(..) : @Annotation; // shouldn't have any errors
	declare @method : public void C.amethod(..) : @Annotation; // already get a warning for this, don't want an error saying method doesn't exist
}

class C {
	@Annotation public void amethod() {		
	}
}

aspect D {
	public void C.anotherMethod() {
	}
}
