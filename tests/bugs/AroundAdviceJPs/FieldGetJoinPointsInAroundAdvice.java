import java.util.*;

public aspect FieldGetJoinPointsInAroundAdvice {

	private static int secretField1;
	private int        secretField2;
	public  static int nonsecretField3;
	public  int        nonsecretField4;
	
	
	static int privateNonstaticFieldGets = 0;
	static int privateStaticFieldGets = 0;
	static int publicNonstaticFieldGets = 0;
	static int publicStaticFieldGets = 0;
	
	before () : cflow(adviceexecution()) && get(private !static * *secret*)         { privateNonstaticFieldGets++; tjps.add(thisJoinPoint.getSourceLocation());}
	before () : cflow(adviceexecution()) && get(private static * *secret*)  { privateStaticFieldGets++;}
	before () : cflow(adviceexecution()) && get(public !static * *secret*)         { publicNonstaticFieldGets++;}
	before () : cflow(adviceexecution()) && get(public static * *secret*)  { publicStaticFieldGets++;}
	
	pointcut execTest () : execution(* FieldGetJoinPointsInAroundAdvice.test());
	
	before () : execTest() {
		int i = secretField1;
		i=secretField2;
		i=nonsecretField3;
		i=nonsecretField4;
	}
	
	void around () : execTest() {
		int i=secretField1;
		i=secretField2;
		i=nonsecretField3;
		i=nonsecretField4;
		proceed();
	}
	
	after () : execTest () {
		int i=secretField1;
		i=secretField2;
		i=nonsecretField3;
		i=nonsecretField4;
	}
	
	private static List tjps = new ArrayList();
	
	public static void test () {
      System.out.println("? test()");
	}
	
	public static void main (String[] args) {
		test();		
		if (privateNonstaticFieldGets!=privateStaticFieldGets || 
		    privateStaticFieldGets!=publicStaticFieldGets ||
			publicStaticFieldGets!=publicNonstaticFieldGets) throw new RuntimeException(
					"\n privateNonstaticFieldGets="+privateNonstaticFieldGets+
					"\n publicNonstaticFieldGets="+publicNonstaticFieldGets+
					"\n privateStaticFieldGets="+privateStaticFieldGets+
					"\n publicStaticFieldGets="+publicStaticFieldGets);
		//System.err.println(tjps);
	}
}