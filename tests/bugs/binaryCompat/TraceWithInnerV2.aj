aspect Trace {
	public static boolean expectNoSuchMethodError = false;
	
	before(): execution(void doit(..)) {
		System.out.println("entering");
		
	}

	public void method() {
		// Extra method to do nothing but test if the numbering still behaves	
	}

	static aspect InnerTrace {
		before(): execution(void doit(..)) {
			System.out.println("Inner entering");
		}
		
		after() returning: execution(void doit(..)) {
			System.out.println("Inner exiting");
		}
		
		after() throwing: execution(void doit(..)) {
			System.out.println("Inner chucking");
		}
		
		before(): execution(* noMatch(..)) {
			System.out.println("This doesn't match anything, but checks the sequence number for the next bit of advice is OK");
		}
	}
	
	after() returning: execution(void doit(..)) {
		System.out.println("exiting");
	}
}