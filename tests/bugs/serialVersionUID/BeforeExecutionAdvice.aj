public aspect BeforeExecutionAdvice {
	
	pointcut run (Test test) :
		execution(public void run()) && this(test);
		
	before (Test test) : run (test) {
		System.out.println("? BeforeExecutionAdvice.run() test=" + test);
	}
}