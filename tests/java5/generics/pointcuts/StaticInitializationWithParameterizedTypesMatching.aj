public aspect StaticInitializationWithParameterizedTypesMatching {
	
	pointcut allowedStaticInit() : staticinitialization(GenericInterface<Double>+);
	
	pointcut allowedStaticInitClass() : staticinitialization(GenericImplementingClass<Double>+);
	
	// matches ConcreteImplementingClass
	// matches ConcreteExtendingClass
	declare warning : allowedStaticInit() : "clinit(GenericInterface<Double>+)";
	
	// matches ConcreteExtendingClass
	declare warning : allowedStaticInitClass() : "clinit(GenericImplementingClass<Double>+)";
	
	// no matches
	declare warning : staticinitialization(GenericInterface<String>+) :
		"should not match";
	
	// no matches
	declare warning : staticinitialization(GenericInterface<Double,Double>+) :
		"should not match";
}