public aspect GetAndSetPointcutMatchingFieldType {
	
	Generic<String> gs = new Generic<String>();
	
	// rule 3) a raw field type pattern matches any parameterized  type
	declare warning : get(Generic *.*) && withincode(* rawFieldTypePatternMatching())
	                  : "raw field type matching in get ok";
	declare warning : set(Generic *.*) && withincode(* rawFieldTypePatternMatching())
	                  : "raw field type matching in set ok";
	
	
	void rawFieldTypePatternMatching() {
		System.out.println(gs);  // get
		gs = null;  // set
	}
		
	// rule 4) A field type declared using a type variable is matched by its erasure
	declare warning : get(Object foo) : "erasure matching in get ok";
	declare warning : set(Object foo) : "erasure matching in set ok";
	declare warning : get(java.util.List<Object> foos) : "does not match! erasure is List";
	declare warning : set(java.util.List<Object> foos) : "does not match! erasure is List";
	declare warning : get(java.util.List foos) : "erasure matching in get with params ok";
	declare warning : set(java.util.List foos) : "erasure matching in set with params ok";
	
	// rule 5) A field type declared using a parameterized type is matched by parameterized type patterns
	declare warning : get(java.util.List<String> *) : "parameterized type matching in get ok";
	declare warning : set(java.util.List<String> *) : "parameterized type matching in set ok";
	declare warning : get(java.util.Map<Number,String> *) : "parameterized type matching in get ok x2";
	declare warning : set(java.util.Map<Number,String> *) : "parameterized type matching in set ok x2";
	
	// rule 6) generic wildcards match exactly, aspectj wildcards match wildly
	declare warning : get(java.util.List<?> *) : "wildcard get matching ok";
	declare warning : set(java.util.List<?> *) : "wildcard set matching ok";
	declare warning : get(java.util.List<? extends Number> *) : "wildcard extends get matching ok";
	declare warning : set(java.util.List<? extends Number> *) : "wildcard extends set matching ok";
	declare warning : get(java.util.List<? super Number> *) : "wildcard super get matching ok";
	declare warning : set(java.util.List<? super Number> *) : "wildcard super set matching ok";
	
	declare warning : get(java.util.List<*> *) : "the really wild show";
}


class Generic<T> {
	
	public T foo = null;
	private java.util.List<T> foos = null;
	
	T getFoo() {
		return foo;
	}
	
	void bar() {
		Object x = foos;
	}
	
}

interface ISore<E> {
	
	public static final int x = 5;
	
	void iSee(E anE);
	
}

class UglyBuilding /*implements ISore<String>*/ {
	
	java.util.List<String> ls;
	java.util.Map<Number,String> mns;
	
	public void iSee(String s) {
		ls = null;  // set
		Object o = ls; // get
		mns = null;  // set
		o = mns;  // get
	}
	
	java.util.List<?> wl;
	java.util.List<? extends Number> snl;
	java.util.List<? super Number> nl;
	
	void makeSomeJPs() {
		wl = null;
		Object o = wl;
		snl = null;
		o = snl;
		nl = null;
		o = nl;
	}
}