public aspect AfterReturningStaticinitializationAdvice {

	pointcut staticInit () :
		staticinitialization(*Test);
	
	after () returning : staticInit() {
		System.out.println("? AfterReturningStaticinitializationAdvice.staticInit()");
	}
}
