package abc;
import def.*;

// This time, the around advice calls the private static method foo but the around advice
// will be inlined into a type in a different package (PrivateCall3).  This should work
// as the around advice will call back to the aspect which will call on to foo().

public class PrivateCall_Instance_Package1 {

	public void test () {foo("test");}
	
	private void foo (String from) {
		System.err.print(":"+from);
	}
	
	public static void main(String[] args) {
		new PrivateCall_Instance_Package1().test();
	}
	
	private static aspect Aspect {
		pointcut execTest (PrivateCall_Instance_Package2 o) : execution(* PrivateCall_Instance_Package2.test()) && target(o);
		before (PrivateCall_Instance_Package2 o)      :  execTest (o) { new PrivateCall_Instance_Package1().foo("before"); }
		void around (PrivateCall_Instance_Package2 o) :  execTest (o) { new PrivateCall_Instance_Package1().foo("around"); }
	}
}