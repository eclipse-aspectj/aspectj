import java.util.*;

public aspect JoinPointInAroundAdvice {

	static int i = 0;
	static int j = 0;
	
	before () : call(* JoinPointInAroundAdvice.privateMethod(..)) {	i++; tjps.add(thisJoinPoint.getSourceLocation());}
	before () : call(* JoinPointInAroundAdvice.publicMethod(..))  { j++;}
	
	pointcut execTest () : execution(* JoinPointInAroundAdvice.test());
	
	before () : execTest() {
		privateMethod("before");
		publicMethod("before");
	}
	
	void around () : execTest() {
		privateMethod("around");
		publicMethod("around");
		proceed();
	}
	
	after () : execTest () {
		privateMethod("after");
		publicMethod("after");
	}
	
	private static List tjps = new ArrayList();
	
	private static void privateMethod(String from) { }//System.out.println("? privateMethod() " + from); }
	public  static void publicMethod(String from)  { }//System.out.println("? publicMethod() " + from);  }
	
	public static void test () {
      System.out.println("? test()");
	}
	
	public static void main (String[] args) {
		test();		
		if (i!=j || i!=3) throw new RuntimeException("Missing join point: private="+i+" public="+j);
		//System.err.println(tjps);
	}
}

