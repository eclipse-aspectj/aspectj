aspect Trace {
	public static boolean expectNoSuchMethodError = false;
	
	before(): execution(void doit(..)) {
		System.out.println("enter");
	}

	static aspect InnerTrace {
		before(): execution(void doit(..)) {
			System.out.println("Inner enter");
		}
		
		after() returning: execution(void doit(..)) {
			System.out.println("Inner exit");
		}
		
		after() throwing: execution(void doit(..)) {
			System.out.println("Inner after throwing");
		}
	}
	after() returning: execution(void doit(..)) {
		System.out.println("exit");
	}
}