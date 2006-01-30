

public abstract aspect AbstractMethods {

	protected abstract pointcut tracingScope ();
	
	before () : tracingScope () {
		test();
	}
	
	protected abstract void test ();
//	protected void test () {}
}
