aspect Trace {
	public static boolean expectNoSuchMethodError = false;
	
	before(): execution(void doit(..)) {
		System.out.println("enter");
	}
	
	after() returning: execution(void doit(..)) {
		System.out.println("exit");
	}
}