// Exploring synchronization

aspect LockMonitor {
	long locktimer = 0;
	int iterations = 0;
	Object currentObject = null;
	long activeTimer;
	
	before(Useful2 o ): lock() && args(o) {
		activeTimer = System.currentTimeMillis();
		currentObject = o;
	}
	
	after(Useful2 o ): unlock() && args(o) {
		if (o!=currentObject) { 
			throw new RuntimeException("Unlocking on incorrect thing?!?");
		}
		if (activeTimer!=0) {
			locktimer+=(System.currentTimeMillis()-activeTimer);
			iterations++;
			activeTimer=0;
		}
	}
	
	after() returning: execution(* main(..)) {
		System.err.println("Average time spent with lock over "+iterations+" iterations is "+
				(((double)locktimer)/
				 ((double)iterations))+"ms");
	}
}

public class Useful2 {
	public static void main(String[] args) {
		Useful2 u = new Useful2();
		
		for (int i = 0; i < 20; i++) {
			u.methodWithSynchronizedBlock();
		}
	}
	
	public void methodWithSynchronizedBlock() {
		synchronized (this) {
			for (int ii=0;ii<1000000;ii++);
		}
	}

}
