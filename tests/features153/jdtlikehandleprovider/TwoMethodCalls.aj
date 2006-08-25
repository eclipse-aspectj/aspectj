aspect A {

	pointcut tracedPrint(String s): call(void java.io.PrintStream.println(*)) &&
		args(s) && !within(A);
		
	before(String s): tracedPrint(s) {
		System.out.println("got you: " + s + " ;)");
	}	
		
	after(String s): tracedPrint(s) {
		System.out.println("hehe, finished: " + s + " :(");
	}
}

class Main {

	public static void main(String[] args) {
		System.out.println("start");
		System.out.println("end");
	}
	
}

