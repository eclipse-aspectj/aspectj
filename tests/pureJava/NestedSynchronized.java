public class NestedSynchronized {
    static Object lockA = new Object();
    static Object lockB = new Object();

    static int bug() {
	synchronized (lockA) {
	    synchronized (lockB) {
		return 0;
	    }
	}
    }


    public static void main(String[] args) {
	bug();
    }
}

