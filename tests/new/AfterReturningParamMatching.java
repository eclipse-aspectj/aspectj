import org.aspectj.testing.Tester;

// this test verifies the matching behaivor for after returning with a typed parameter.

public class AfterReturningParamMatching {
	public static void main(String[] args) {
		goBoolean(false);
		Tester.checkAndClearEvents(new String[] { "boolean", "Object" });

		goByte(1);
		Tester.checkAndClearEvents(new String[] { "byte", "int", "long", "Object"});
		
		goInt(2);
		Tester.checkAndClearEvents(new String[] { "int", "long", "Object" });

		goLong(3);
		Tester.checkAndClearEvents(new String[] { "long", "Object" });
		
		goObject(new Object());
		Tester.checkAndClearEvents(new String[] { "Object" });
		
	    goObject(new Integer(4));
		Tester.checkAndClearEvents(new String[] { "Object", "Number", "Integer" });
		
		goObject(null); 
		Tester.checkAndClearEvents(new String[] { "Object" });
	    
		goNumber(new Long(5));
		Tester.checkAndClearEvents(new String[] { "Object", "Number" });
		
		goNumber(new Integer(6));
		Tester.checkAndClearEvents(new String[] { "Object", "Number", "Integer" });

		goNumber(null);
		Tester.checkAndClearEvents(new String[] { "Object", "Number" });

		goInteger(new Integer(7));
		Tester.checkAndClearEvents(new String[] { "Object", "Number", "Integer" });

        goInteger(null);
        Tester.checkAndClearEvents(new String[] { "Object", "Number", "Integer" });

	}
	static boolean goBoolean(boolean b) { return b; }
	static byte goByte(int i) { return (byte) i; }
	static int goInt(int i) { return i; }
	static long goLong(int i) { return (long) i; }
	
	static Object goObject(Object o) { return o; }
	static Number goNumber(Number o) { return o; }
	static Integer goInteger(Integer o) { return o; }
}

aspect A {
	
	pointcut methodsInQuestion():
		call(* goBoolean(*)) || 
		call(* goByte(*)) || 
		call(* goInt(*)) || 
		call(* goLong(*)) || 
		call(* goObject(*)) || 
		call(* goNumber(*)) || 
		call(* goInteger(*)); 
	
	after() returning(boolean b): methodsInQuestion() { Tester.event("boolean"); }
	after() returning(byte b): methodsInQuestion() { Tester.event("byte"); }
	after() returning(int b): methodsInQuestion() { Tester.event("int"); }
	after() returning(long b): methodsInQuestion() { Tester.event("long"); }
	after() returning(Object b): methodsInQuestion() { Tester.event("Object"); }
	after() returning(Number b): methodsInQuestion() { Tester.event("Number"); }
	after() returning(Integer b): methodsInQuestion() { Tester.event("Integer"); }

}