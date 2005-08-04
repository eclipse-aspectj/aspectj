public aspect WithinCodeOverriding {
	
	// if a type overrides a generic method from a supertype, changing the
	// signature in the process (for example, is a generic subtype with a 
	// narrowed type variable, or extends a parameterized super class, or
	// implements a parameterized interface), then a type pattern of
	// OriginalDeclaringType.erasureOfOriginalSignature matches, and a 
	// type pattern of *.erasureOfOriginalSignature matches, but
	// a type pattern OverridingType.erasureOfOriginalSignature DOES NOT
	// MATCH.
	
	declare warning : withincode(void *.foo(Object))
	                  : "wildcard declaring type match on erasure";
	
	declare warning : withincode(void Generic.foo(Object))
	                  : "base declaring type match on erasure";
	
	declare warning : withincode(void SubGeneric.foo(Object))
	                  : "not expecting any matches";
	
	declare warning : withincode(void SubGeneric.foo(Number))
	                  : "sub type match on erasure";
	
	declare warning : withincode(void SubParameterized.foo(Object))
					  : "not expecting any matches";
	
	declare warning : withincode(void SubParameterized.foo(String))
					  : "parameterized match on erasure";
}

class Generic<T> {
	int x = 0;
	
	// withincode (void Generic.foo(Object))
	// withincode (void *.foo(Object))
	public void foo(T someObject) {
		x = 1;
	}
	
}

class SubGeneric<N extends Number> extends Generic<N> {
	int y = 0;
	
	// withincode(void Generic.foo(Object))
	// withincode( void *.foo(Object))
	// withincode(void SubGeneric.foo(Number))
	// !withincode(void SubGeneric.foo(Object))
	public void foo(N someObject) {
		y = 1;
	}
	
}

class SubParameterized extends Generic<String> {
	int y = 0;
	
	// withincode(void Generic.foo(Object))
	// withincode( void *.foo(Object))
	// withincode(void SubParameterized.foo(String))
	// !withincode(void SubGeneric.foo(Object))
	public void foo(String someObject) {
		y = 1;
	}
		
}

interface I<E> {
	void bar(E anElement);	
}

class ParameterizedI implements I<Double> {
	int x;
	
	// withincode(void I.bar(Object))
	// withincode(void *.bar(Object))
	// withincode(void ParameterizedI.bar(Double))
	// !withincode(void ParameterizedI.bar(Object))
	public void bar(Double d) {
		x = 1;
	}
	
	static aspect ParameterizedChecker {
		
		declare warning : withincode(void I.bar(Object)) : "erasure match on base interface";
		declare warning : withincode(void *.bar(Object)) : "wildcard match on erasure";
		declare warning : withincode(void ParameterizedI.bar(Double)) : "parameterized match";
		declare warning : withincode(void ParameterizedI.bar(Object)) : "no match expected";
	}
}

