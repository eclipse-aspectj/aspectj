aspect Trace {
	public static boolean expectNoSuchMethodError = true;
	
	before(): execution(void main(..)) {  // expect an error for incompatible binary change
		System.out.println("enter");
	}
	
	after() returning: execution(void doit(..)) {
		System.out.println("exit");
	}
}