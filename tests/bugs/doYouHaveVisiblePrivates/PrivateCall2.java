package abc;

// This time, the around advice calls the private static method foo but the around advice
// will be inlined into a type in a different package (PrivateCall3).  This should work
// as the around advice will call back to the aspect which will call on to foo().

public class PrivateCall2 {

	public void test () {foo("test");}
	
	private static void foo (String from) {
		System.err.print(":"+from);
	}
	
	public static void main(String[] args) {
		new PrivateCall2().test();
	}
	
	private static aspect Aspect {
		pointcut execTest () : execution(* test());
		before ()      :  execTest () { foo("before"); }
		void around () :  execTest () { foo("around"); }
	}
}