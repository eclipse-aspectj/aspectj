import org.aspectj.testing.Tester;

/** @testcase PR#601 VerifyError if nested sync returning result */
public class NestedSyncWithResult {
	public static boolean holdA;
	public static boolean aWaiterDone;
	public static boolean aHolderDone;
	public static void main(String[] args) {
		int result = Bug.bug();
		Tester.check(0 == result, "0 == result");
		checkSynchronization();
		Tester.checkAllEvents();
	}

	public static void checkSynchronization() {
		final StringBuffer sb = new StringBuffer();
		Tester.expectEvent("holding A; releasing A; invoked bug; ");
		final boolean[] holdAstarted = new boolean[] { false };
		holdA = true;
		Runnable aHolder = new Runnable() {
			public void run() {
				boolean wroteWait = false;
				synchronized (Bug.lockB) {
					holdAstarted[0] = true;
					while (holdA) {
						if (!wroteWait) {
							wroteWait = true;
							sb.append("holding A; ");
						}
						sleep();
					}
					sb.append("releasing A; ");
				}
				aHolderDone = true;
			}
		};
		Runnable aWaiter = new Runnable() {
			public void run() {
				while (!holdAstarted[0]) {
					sleep();
				}
				Bug.bug();
				sb.append("invoked bug; ");
				aWaiterDone = true;
			}
		};
		new Thread(aHolder).start();
		new Thread(aWaiter).start();
		sleep();
		holdA = false;
		while (!aWaiterDone && !aHolderDone) {
			sleep();
		}
		Tester.event(sb.toString());
		//System.err.println("got: " + sb.toString());
	}

	public static void sleep() {
		try {
			Thread.currentThread().sleep(300);
		} catch (InterruptedException e) {
		} // ignore
	}
}

class Bug {
	public static Object lockA = new Object();
	public static Object lockB = new Object();

	static int bug() {
		synchronized (lockA) {
			synchronized (lockB) {
				return 0;
			}
		}
	}
}
