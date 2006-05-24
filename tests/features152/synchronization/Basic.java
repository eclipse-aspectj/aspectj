// Exploring synchronization

public class Basic {
	public static void main(String[] args) {
		Basic b = new Basic();
		
		b.methodWithSyncBlock1();
		b.staticMethodWithSyncBlock1();
		b.methodWithSyncBlock2();
		b.staticMethodWithSyncBlock2();
	}
	
	public void methodWithSyncBlock1() {
		System.err.println("methodWithSyncBlock1");
		synchronized (this) {
		}
	}

	public void staticMethodWithSyncBlock1() {
		System.err.println("staticMethodWithSyncBlock1");
		synchronized (Basic.class) {
		}
	}
	
	public void methodWithSyncBlock2() {
		System.err.println("methodWithSyncBlock2");
		synchronized (this) {
			int i = 0;
			while (i<100) {
				i++;
			}
		}
	}

	public void staticMethodWithSyncBlock2() {
		System.err.println("staticMethodWithSyncBlock2");
		synchronized (Basic.class) {
			int i = 0;
			while (i<100) {
				i++;
			}
		}
	}
}
