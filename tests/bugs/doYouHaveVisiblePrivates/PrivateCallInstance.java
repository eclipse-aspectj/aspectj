// Similar to PrivateCall but now foo() is a private non-static method.
public class PrivateCallInstance {

	public void test () {foo("test");}
	
	private void foo (String from) {
		System.err.print(":"+from);
	}
	
	public static void main(String[] args) {
		new PrivateCallInstance().test();
	}
	
	private static aspect Aspect {
		
		pointcut execTest (PrivateCallInstance s) :
			execution(* PrivateCallInstance.test()) && target(s);
		
		before (PrivateCallInstance s) :  execTest (s) {
			s.foo("before");
		}
		
		void around (PrivateCallInstance s) :  execTest (s) {
			s.foo("around");
		}
	}
}