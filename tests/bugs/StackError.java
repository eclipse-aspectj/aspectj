import java.lang.reflect.Method;

public class StackError {
	public static void main(String args[]) {
		new StackError().testEqualsNull();
	}

	void assertTrue(String msg, boolean b) {}

	public void testEqualsNull() {
		StackError one = new StackError();
		StackError two = new StackError();
		assertTrue("equal", one.equals(two));	// does not work
		//boolean yes = one.equals(two);			// works
	}
	
	public boolean equals(Object other) {
		return true;
	}
}

aspect EqualsContract {
	pointcut equalsCall(Object thisOne, Object otherOne):
		target(Object+) && 
 
	target(thisOne) &&
		call(public boolean equals(Object+)) &&
		args(otherOne) &&
		!within(EqualsContract);
	
	boolean around(Object thisOne, Object otherOne):
	equalsCall(thisOne, otherOne) {
		boolean result = proceed(thisOne, otherOne);
		Class cls = thisOne.getClass();
		String name = cls.getName();
		boolean hasHashCode = false;
		try {
			Method m = cls.getDeclaredMethod("hashCode", null);
			String lookFor = "public int " + name + ".hashCode()";
			hasHashCode = lookFor.equals(m.toString());
		}
		catch (NoSuchMethodException nsme) {
		}
		return result;
	}
}
