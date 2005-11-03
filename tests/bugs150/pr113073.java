@interface Annotation{}
aspect B {
	declare @method : public * C.anotherMethod(..) : @Annotation; // should be woven
	declare @constructor : C.new(String) : @Annotation;
}

class C {
}

aspect D {
	public C.new(String s) {
		this();
	}
	public void C.anotherMethod(String s) {
	}
	public void C.anotherMethod() {
	}
}
