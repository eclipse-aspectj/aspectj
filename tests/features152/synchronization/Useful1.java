// Exploring synchronization

aspect WithinAspect {
	long locktimer = 0;
	int iterations = 0;
	Object currentObject = null;
	boolean didSomething = false;
	long activeTimer;
	
	before(Object o ): within(Useful1) && args(o) {
		if (thisJoinPoint.getSignature().toString().startsWith("lock(")) {
			activeTimer = System.currentTimeMillis();
			didSomething = true;
		}
	}
	
	after(Object o ): within(Useful1) && args(o) {
		if (thisJoinPoint.getSignature().toString().startsWith("unlock(")) {
			if (activeTimer!=0) {
				locktimer+=(System.currentTimeMillis()-activeTimer);
				iterations++;
				activeTimer=0;
				didSomething = true;
			}
		}
	}
	
	after() returning: execution(* main(..)) {
		System.err.println("Average lock taking time over "+iterations+" iterations is "+
				(((double)locktimer)/
				 ((double)iterations))+"ms");
		if (didSomething) System.err.println("We did time something!"); // can write a test looking for this line, it won't vary
	}
}

public class Useful1 {
	public static void main(String[] args) {
		Useful1 u = new Useful1();
		
		for (int i = 0; i < 2000; i++) {
			u.methodWithSynchronizedBlock();
		}
	}
	
	public void methodWithSynchronizedBlock() {
		synchronized (this) {
			for (int ii=0;ii<100;ii++);
		}
	}

}
