
// In this program, the around advice calls foo() and foo is a private static field in 
// class PrivateCall.  When compiled the around() advice will be inlined and should call
// foo() through an inline accessor method.
public class PrivateCall {

	public void test () {foo("test");}
	
	private static void foo (String from) {
		System.err.print(":"+from);
	}
	
	public static void main(String[] args) {
		new PrivateCall().test();
	}
	
	private static aspect Aspect {
		
		pointcut execTest () :
			execution(* PrivateCall.test());
		
		before () :  execTest () {
			foo("before");
		}
		
		void around () :  execTest () {
			foo("around");
		}
	}
}