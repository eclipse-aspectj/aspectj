public aspect StaticInitializationWithParameterizedTypes {
	
	// CE line 4
	pointcut badStaticInit() : staticinitialization(GenericInterface<Double>);
	// CE line 6
	pointcut allowedStaticInit() : staticinitialization(GenericInterface<Double>+);
	
	// CE line 9
	pointcut badStaticInitClass() : staticinitialization(GenericImplementingClass<Double>);
	// CE line 10
	pointcut allowedStaticInitClass() : staticinitialization(GenericImplementingClass<Double>+);
	
	// CE line 14
	pointcut sneakItIntoDisjunction() : staticinitialization(String || GenericInterface<Double>);
}