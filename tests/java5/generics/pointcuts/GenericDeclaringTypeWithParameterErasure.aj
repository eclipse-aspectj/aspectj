public aspect GenericDeclaringTypeWithParameterErasure {
	
	// tests that the erasure of parameter types does NOT match a member when
	// the declaring type pattern is generic (must use the type variable instead in
	// the non-raw [cooked?] world).
	
	void someCode() {
		ConcreteImplementingClass cic = new ConcreteImplementingClass();
		cic.asInt(5.0d);  
		GenericImplementingClass<Long> gic = new GenericImplementingClass<Long>();
		gic.asInt(55L);   
	}
	
	declare warning : 
		execution<N>(* GenericInterface<N extends Number>.asInt(Number)) :
		"execution<N>(* GenericInterface<N>.asInt(Number))";
	
}