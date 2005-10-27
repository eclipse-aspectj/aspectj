@interface Annotation{}
aspect B {
	declare @constructor : C.new(String) : @Annotation; // shouldn't have any errors
	declare @constructor : *.new(int) : @Annotation; // shouldn't have any errors
	declare @constructor : *.new(int) : @Annotation; // already get a warning for this, don't want an error saying method doesn't exist
}

class C {
	@Annotation public C(int i) {
	}
}

aspect D {
	public C.new(String s){}
}
