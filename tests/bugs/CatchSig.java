/*
 * Bugzilla Bug 37739  
   Unexpected Xlint:unresolvableMember warning with withincode 
 */
public class CatchSig {
	CatchSig(Class type) {}

	CatchSig() {
		this(String.class);
	}
	
	public static void main(String[] args) {
		new CatchSig();
		new B().test();
		new B().test2();
		B.findClass();
	}
}

class B extends CatchSig {
	public B() {
		super(findClass());
	}
	
	static Class findClass() {
		return B.class;
	}

	public void test() {
	}

	public void test2() {
		test();
	}
}

aspect C {
	void around() :
		(call (void B.test()) &&
		 withincode (void B.test2())) {
		System.out.println("test from test2");
		proceed();
	}
	
	before(): call(Class B.findClass()) {
		System.out.println("from: " + thisEnclosingJoinPointStaticPart);
	}
	before(): call(Class B.findClass()) && withincode(B.new()) {
		System.out.println("from B.new()");
	}
}
