public aspect AroundExecutionAdvice {
	
	pointcut run () :
		execution(public void run());
		
	void around () : run () {
		System.out.println("> AroundExecutionAdvice.run()");
		
		proceed();
		
		System.out.println("< AroundExecutionAdvice.run()");
	}

}
