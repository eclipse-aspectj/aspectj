
import org.aspectj.testing.Tester;


class C implements Runnable { // CW 5
	public void run() {
	}
}
class F implements Runnable {
	F(int i) {}// CW 10
	
	public void run() {
	}
}

/** @testcase PR#49295 extra warning (join point?) for typepattern-type execution */
public class SubtypeConstructorCW {
	public static void main(String[] args) {
		new C().run();
		new D("").run();
		new E("", 0).run();
		new F(0).run();
	}
}

class D implements Runnable {
	D(String s) {
	}
	public void run() {
	}
}

class E implements Runnable {
	E(String s, int i) {
	}
	public void run() {
	}
}

// XXX warning: harness might ignore duplicates, so this can give false positives
aspect A {
	static {
		Tester.expectEvents(
			new String[] {
				"before execution(C())",
				"before execution(F(int))",
				});
	}
	static void event(String s) {
		System.out.println("    \"" + s + "\",");
	}
	// getting two warning rather than one, and on wrong places
	declare warning : execution((Runnable +).new (..))
		&& !execution(new (String,
			.
			.)) : "Runnable constructors should take String as first parameter";

	//  this works as expected
	//    declare warning: execution((!Runnable && Runnable+).new(..))
	//        && !execution(new(String, ..)) 
	//        : "Runnable constructors should take String as first parm";

	before() : execution((Runnable +).new (..))
		&& !execution(new (String,..)) {
		event("before " + thisJoinPointStaticPart);
	}
}
