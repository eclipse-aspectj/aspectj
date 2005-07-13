public aspect HandlerPointcutTests {

	// CE line 4 - no type variables allowed in handler
	pointcut exceptionHandler() : handler<T>(GenericInterface<T>);
	
	// CE line 8 - no parameterized types
	// CW line 8 - unresolved absolute type name T
	pointcut unboundTypeInHandler() : handler(GenericInterface<T>);
	
	// CE line 11 - no parameterized types
	pointcut parameterizedHandler() : handler(GenericInterface<Double>);

}