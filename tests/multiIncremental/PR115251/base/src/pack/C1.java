package pack;

public class C1 {
	
	public void testMethod() {
 		new C1();
	}

}

aspect A extends A1<C1> {
	protected pointcut creation() : call(C1.new());
}
