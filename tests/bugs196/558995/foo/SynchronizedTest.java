package foo;

//@ProfileMe
//@ThreadSafe
@Synchronized//(timeout = 2, unit = TimeUnit.SECONDS)
public final class SynchronizedTest { //implements Runnable {


	@Synchronized
    public void incrementCounter() {
/*
        int n = counter;
        log.debug("counter read (" + n + ")");
        ThreadUtil.sleep();
        n++;
        log.debug("counter increased (" + n + ")");
        ThreadUtil.sleep();
        counter = n;
        log.debug("counter written (" + n + ")");
*/
    }

	public void run() {
	//	incrementCounter();
	}

}
