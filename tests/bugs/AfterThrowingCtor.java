// pr44586
import org.aspectj.testing.Tester;
public aspect AfterThrowingCtor {
	after() throwing (Throwable t) : execution(Foo*.new(..)) {
		throw new AdviceRanException();
	}
	
	public static void main(String args[]) {
		try {
			new Foo();
			Tester.checkFailed("Advice should not run here");
		} catch(IllegalStateException illEx) {
			// good, we do not want the advice to run as the
			// initialization of an itd field is considered part
			// of the initialization join point, but not the execution
			// join point.
		}
		try {
			new Foo1();
			Tester.checkFailed("Advice should run here");
		} catch(AdviceRanException arEx) {
			// good, the advice should run as the field initialisation is considered
			// part of the execution join point.
		}
	}

	private Object Foo.val = Foo.initVal();
	
	class AdviceRanException extends RuntimeException {};
}

class Foo {
	Foo() { 
	}

	static Object initVal() {
		throw new IllegalStateException("crash"); 
	}
}

class Foo1 {
	Foo1() { 
	}

	private Object val = initVal();

	static Object initVal() {
		throw new IllegalStateException("crash"); 
	}
}