
public abstract aspect AbstractMethods {

	protected abstract pointcut tracingScope ();
	
	before () : tracingScope () {
		test();
                System.out.println("advice running");
	}
	
	protected abstract void test ();
//	protected void test () {}
}
