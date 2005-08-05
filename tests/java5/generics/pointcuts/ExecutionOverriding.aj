public aspect ExecutionOverriding {
	
	// if a type overrides a generic method from a supertype, changing the
	// signature in the process (for example, is a generic subtype with a 
	// narrowed type variable, or extends a parameterized super class, or
	// implements a parameterized interface), then a type pattern of
	// OriginalDeclaringType.erasureOfOriginalSignature matches, and a 
	// type pattern of *.erasureOfOriginalSignature matches, but
	// a type pattern OverridingType.erasureOfOriginalSignature DOES NOT
	// MATCH.
	
	declare warning : execution(void *.foo(Object))
	                  : "wildcard declaring type match on erasure";
	
	declare warning : execution(void Generic.foo(Object))
	                  : "base declaring type match on erasure";
	
	declare warning : execution(void SubGeneric.foo(Object))
	                  : "not expecting any matches";
	
	declare warning : execution(void SubGeneric.foo(Number))
	                  : "sub type match on erasure";
	
	declare warning : execution(void SubParameterized.foo(Object))
					  : "not expecting any matches";
	
	declare warning : execution(void SubParameterized.foo(String))
					  : "parameterized match on erasure";
}

class Generic<T> {
	int x = 0;
	
	// execution (void Generic.foo(Object))
	// execution (void *.foo(Object))
	public void foo(T someObject) {
		x = 1;
	}
	
}

class SubGeneric<N extends Number> extends Generic<N> {
	int y = 0;
	
	// execution(void Generic.foo(Object))
	// execution( void *.foo(Object))
	// execution(void SubGeneric.foo(Number))
	// !execution(void SubGeneric.foo(Object))
	public void foo(N someObject) {
		y = 1;
	}
	
}

class SubParameterized extends Generic<String> {
	int y = 0;
	
	// execution(void Generic.foo(Object))
	// execution( void *.foo(Object))
	// execution(void SubParameterized.foo(String))
	// !execution(void SubGeneric.foo(Object))
	public void foo(String someObject) {
		y = 1;
	}
		
}

interface I<E> {
	void bar(E anElement);	
}

class ParameterizedI implements I<Double> {
	int x;
	
	// execution(void I.bar(Object))
	// execution(void *.bar(Object))
	// execution(void ParameterizedI.bar(Double))
	// !execution(void ParameterizedI.bar(Object))
	public void bar(Double d) {
		x = 1;
	}
	
	static aspect ParameterizedChecker {
		
		declare warning : execution(void I.bar(Object)) : "erasure match on base interface";
		declare warning : execution(void *.bar(Object)) : "wildcard match on erasure";
		declare warning : execution(void ParameterizedI.bar(Double)) : "parameterized match";
		declare warning : execution(void ParameterizedI.bar(Object)) : "no match expected";
	}
}

