import java.util.*;

public aspect FieldJoinPointsInAroundAdvice {

	private static int secretField1;
	private int        secretField2;
	public  static int nonsecretField3;
	public  int        nonsecretField4;
	
	
	static int privateNonstaticFieldSets = 0;
	static int privateStaticFieldSets = 0;
	static int publicNonstaticFieldSets = 0;
	static int publicStaticFieldSets = 0;
	
	before () : cflow(adviceexecution()) && set(private !static * *secret*)         { privateNonstaticFieldSets++; tjps.add(thisJoinPoint.getSourceLocation());}
	before () : cflow(adviceexecution()) && set(private static * *secret*)  { privateStaticFieldSets++; tjps.add(thisJoinPoint.getSourceLocation());}
	before () : cflow(adviceexecution()) && set(public !static * *secret*)         { publicNonstaticFieldSets++;}
	before () : cflow(adviceexecution()) && set(public static * *secret*)  { publicStaticFieldSets++;}
	
	pointcut execTest () : execution(* FieldJoinPointsInAroundAdvice.test());
	
	before () : execTest() {
		secretField1++;
		secretField2++;
		nonsecretField3++;
		nonsecretField4++;
	}
	
	void around () : execTest() {
		secretField1++;
		secretField2++;
		nonsecretField3++;
		nonsecretField4++;
		proceed();
	}
	
	after () : execTest () {
		secretField1++;
		secretField2++;
		nonsecretField3++;
		nonsecretField4++;
	}
	
	private static List tjps = new ArrayList();
	
	public static void test () {
      System.out.println("? test()");
	}
	
	public static void main (String[] args) {
		test();		
		if (privateNonstaticFieldSets!=privateStaticFieldSets || 
		    privateStaticFieldSets!=publicStaticFieldSets ||
			publicStaticFieldSets!=publicNonstaticFieldSets) throw new RuntimeException(
					"\n privateNonstaticFieldSets="+privateNonstaticFieldSets+
					"\n publicNonstaticFieldSets="+publicNonstaticFieldSets+
					"\n privateStaticFieldSets="+privateStaticFieldSets+
					"\n publicStaticFieldSets="+publicStaticFieldSets);
		//System.err.println(tjps);
	}
}