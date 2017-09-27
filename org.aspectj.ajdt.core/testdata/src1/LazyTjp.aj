public class LazyTjp {
	
	private static final int N = 10000000;
	// if lazy tjp is working, then calling the advice that uses thisJoinPoint should
	// take at least this much longer than using an if pcd to bypass the advice
	private static final double minimumRatio = 1.8; // was 8 but jvm seems to be improving all the time!! 
	
	public static void main(String[] args) {
		Trace.enabled = false;
		double tOff = timeIt();  // throw the first result out for warm-up
		tOff = timeIt();
		Trace.enabled = true;
		double tOn = timeIt();
		Trace.enabled = false;
		double tEasy = timeIt0();
		double tGone = timeIt1();
		
		System.out.println("tOff: " + tOff + ", tOn: " + tOn + ", tEasy: " + tEasy + ", tGone: " + tGone);
		System.out.println("ratio: " + tOn/tOff);
		
		Trace.enabled = false;
		double tOff2 = timeIt2();
		tOff2 = timeIt2();
		Trace.enabled = true;
		double tOn2 = timeIt2();
		
		System.out.println("tOff2: " + tOff2 + ", tOn2: " + tOn2);
		System.out.println("ratio2: " + tOn2/tOff2);

		
		if (tOn/tOff < minimumRatio) {
			throw new IllegalStateException("tOn/tOff = " + tOn/tOff + " < " + minimumRatio);
		}
	}
	
	public static double timeIt() {
		long start = System.currentTimeMillis();
	
		for (int i=0; i < N; i++) {
			doit(i);
		}
		
		long stop = System.currentTimeMillis();	
		return (stop-start)/1000.0;	
	}
	
	private static int doit(int x) {
		return x+1;
	}
	
	public static double timeIt0() {
		long start = System.currentTimeMillis();
	
		for (int i=0; i < N; i++) {
			doit0(i);
		}
		
		long stop = System.currentTimeMillis();	
		return (stop-start)/1000.0;	
	}
	
	private static int doit0(int x) {
		return x+1;
	}
	
	public static double timeIt1() {
		long start = System.currentTimeMillis();
	
		for (int i=0; i < N; i++) {
			doit1(i);
		}
		
		long stop = System.currentTimeMillis();	
		return (stop-start)/1000.0;	
	}
	
	private static int doit1(int x) {
		return x+1;
	}
	
	public static double timeIt2() {
		long start = System.currentTimeMillis();
	
		for (int i=0; i < N; i++) {
			doit2(i);
		}
		
		long stop = System.currentTimeMillis();	
		return (stop-start)/1000.0;	
	}
	
	private static int doit2(int x) {
		return x+1;
	}
	
	private static int doit3(int x) {
		return x+1;
	}
}

aspect Trace {
	public static boolean enabled = false;
	
	public static int counter = 0;
	
	pointcut traced(): if (enabled) && execution(* LazyTjp.doit(..));
	
	before(): traced() {
		Object[] args = thisJoinPoint.getArgs();
		counter += args.length;
	}
	
	before(): execution(* LazyTjp.doit0(..)) {
		counter += 1;
	}
	
	pointcut traced2(): if (enabled) && execution(* LazyTjp.doit2(..));
	
	before(): traced2() {
		Object[] args = thisJoinPoint.getArgs();
		counter += args.length;
	}

	after() returning: traced2() {
		Object[] args = thisJoinPoint.getArgs();
		counter += args.length;
	}


	pointcut traced3(): if (enabled) && execution(* LazyTjp.doit3(..));
	
	before(): traced3() {
		Object[] args = thisJoinPoint.getArgs();
		counter += args.length;
	}

	Object around(): traced3() {  // expect Xlint warning in -XlazyTjp mode
		return proceed();
	}


}