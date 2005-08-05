public aspect CallOverriding {
	
	void foo() {
		// make some calls!
		Generic<Number> gn = new Generic<Number>();
		SubGeneric<Double> sgd = new SubGeneric<Double>();
		SubParameterized sp = new SubParameterized();
		gn.foo(new Integer(5));
		sgd.foo(new Double(5));
		sp.foo("hi");
	}
	
	// if a type overrides a generic method from a supertype, changing the
	// signature in the process (for example, is a generic subtype with a 
	// narrowed type variable, or extends a parameterized super class, or
	// implements a parameterized interface), then a type pattern of
	// OriginalDeclaringType.erasureOfOriginalSignature matches, and a 
	// type pattern of *.erasureOfOriginalSignature matches, but
	// a type pattern OverridingType.erasureOfOriginalSignature DOES NOT
	// MATCH.
	
	declare warning : call(void *.foo(Object))
	                  : "wildcard declaring type match on erasure";
	
	declare warning : call(void Generic.foo(Object))
	                  : "base declaring type match on erasure";
	
	declare warning : call(void SubGeneric.foo(Object))
	                  : "not expecting any matches";
	
	declare warning : call(void SubGeneric.foo(Number))
	                  : "sub type match on erasure";
	
	declare warning : call(void SubParameterized.foo(Object))
					  : "not expecting any matches";
	
	declare warning : call(void SubParameterized.foo(String))
					  : "parameterized match on erasure";
}

class Generic<T> {
	int x = 0;
	
	// call (void Generic.foo(Object))
	// call (void *.foo(Object))
	public void foo(T someObject) {
		x = 1;
	}
	
}

class SubGeneric<N extends Number> extends Generic<N> {
	int y = 0;
	
	// call(void Generic.foo(Object))
	// call( void *.foo(Object))
	// call(void SubGeneric.foo(Number))
	// !call(void SubGeneric.foo(Object))
	public void foo(N someObject) {
		y = 1;
	}
	
}

class SubParameterized extends Generic<String> {
	int y = 0;
	
	// call(void Generic.foo(Object))
	// call( void *.foo(Object))
	// call(void SubParameterized.foo(String))
	// !call(void SubGeneric.foo(Object))
	public void foo(String someObject) {
		y = 1;
	}
		
}

interface I<E> {
	void bar(E anElement);	
}

class ParameterizedI implements I<Double> {
	int x;
	
	void foo() {
		ParameterizedI pi = new ParameterizedI();
		pi.bar(5.0d);
	}
	
	// call(void I.bar(Object))
	// call(void *.bar(Object))
	// call(void ParameterizedI.bar(Double))
	// !call(void ParameterizedI.bar(Object))
	public void bar(Double d) {
		x = 1;
	}
	
	static aspect ParameterizedChecker {
		
		declare warning : call(void I.bar(Object)) : "erasure match on base interface";
		declare warning : call(void *.bar(Object)) : "wildcard match on erasure";
		declare warning : call(void ParameterizedI.bar(Double)) : "parameterized match";
		declare warning : call(void ParameterizedI.bar(Object)) : "no match expected";
	}
}

