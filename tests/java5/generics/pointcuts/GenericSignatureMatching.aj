public aspect GenericSignatureMatching {
	
	// tests that references to a generic or parameterized type are
	// always matched by a type pattern refering to the raw type form
	
	void someCode() {
		ConcreteImplementingClass cic = new ConcreteImplementingClass();
		cic.asInt(5.0d);  
		GenericImplementingClass<Long> gic = new GenericImplementingClass<Long>();
		gic.asInt(55L);   
	}
	
	declare warning : 
		execution<T>(* GenericInterface<T extends Number>.asInt(T)) :
		"execution<T>(* GenericInterface<T extends Number>.asInt(T))";
	
}