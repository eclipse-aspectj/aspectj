public aspect PerThisAspect perthis(this(Test)) {
	
	pointcut run (Test test) :
		execution(public void run()) && this(test);
		
	before (Test test) : run (test) {
		System.out.println("? PerThisAspect.run() test=" + test + " this=" + this);
	}

}
