// for Bugzilla Bug 34858  
//   Weaver crash w/ coverage

import org.aspectj.testing.Tester;

public class CflowBinding {
	public static void main(String[] args) {
		new Bar().bar(10);
	}
	
	
	static aspect A {
		pointcut flow(int i, Object o): cflow(execution(void bar(int)) && this(o) && args(i));
		
		Object around() : call(void m()) && flow(int, Object) {
			return proceed();
		}
		
		Object around(final int i) : call(void m()) && flow(i, Object) {
			System.out.println("i: " + i);
			return proceed(i);
		}
		
		Object around(final Object o) : call(void m()) && flow(int, o) {
			System.out.println("o: " + o);
			return proceed(o);
		}
		
		Object around(final Object o, final int i) : call(void m()) && flow(i, o) {
			System.out.println("o: " + o + ", i: " + i);
			return proceed(o, i);
		}
	}
}

class Bar {
	void bar(int i) {
		m();
	}
	void m() {
		System.out.println("m");
	}
}

