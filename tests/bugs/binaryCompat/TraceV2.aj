aspect Trace {
	public static boolean expectNoSuchMethodError = false;
	
	before(): execution(void doit(..)) {
		System.out.println("entering");
		
	}
	
	
	after() returning: execution(void doit(..)) {
		System.out.println("exiting");
	}
}