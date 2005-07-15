public aspect StaticInitializationWithParameterizedTypes {
	
	// CE line 4
	pointcut badStaticInit() : staticinitialization(GenericInterface<Double>);
	
	pointcut allowedStaticInit() : staticinitialization(GenericInterface<Double>+);
	
	// CE line 9
	pointcut badStaticInitClass() : staticinitialization(GenericImplementingClass<Double>);
	
	pointcut allowedStaticInitClass() : staticinitialization(GenericImplementingClass<Double>+);
}