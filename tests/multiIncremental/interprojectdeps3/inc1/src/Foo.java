public class Foo {
    private Object f;
    class MyWorker {
	Object construct() {
	    return Foo.this.f;
	}
    }
}
