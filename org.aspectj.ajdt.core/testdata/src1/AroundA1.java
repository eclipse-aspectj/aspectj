public class AroundA1 {
	
	public static void main(String[] args) {
		System.err.println(new AroundA1().main(3, (short)1, true));
	}
	
	public int main(int xx, short s, boolean yy) {
		System.err.println(xx);
		System.err.println(s);
		System.err.println(yy);
		return 10;
	}
}		

aspect MyAroundAspect {
	int field = 10;
	
	pointcut foo(): call(int main(..));
	
	Object around(AroundA1 o, int i, boolean b): target(o) && args(i, *, b) && foo() {
		System.err.println("enter: " + o + " with " + field);
		Object x = proceed(o, 10, false);
		System.err.println("got: " + x);
		return new Integer(42);
	}
	
	
//	void around(Object a): args(a) && foo() {
//		System.out.println("enter");
//		proceed("new: " + a);
//		System.out.println("exit");
//	}
//	
//	void around(final String[] a): args(a) && foo() {
//		Runnable r = new Runnable() {
//			public void run() {
//				proceed(a);
//			}
//		};
//		r.run();
//		r.run();
//	}
}
	
	