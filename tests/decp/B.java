public class B extends A {
	
	public static void main(String[] args) {
		B b = new B();
		if (b.getClass().getInterfaces().length>0) {
			throw new RuntimeException("B should not implement any interfaces: "+b.getClass().getInterfaces()[0].toString());
		}
		if (!(b instanceof java.io.Serializable)) {
			throw new RuntimeException("B should be serializable! Inherited from A");
		}
	}
}
