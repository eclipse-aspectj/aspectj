public class Handler {
	public static void main(String[] args) {
		m();
	}
	
//	public static void m() {
//		while(true) {
//			foo: {try {
//				int x = 2+3;
//				if (x >3) break foo;
//				x = x+10;
//				//return;
//			} catch (Throwable t) {
//				System.err.println(t);
//			}}
//			System.out.println("still in loop");
//		}
//		//System.out.println("outside");
//		
//	}

	public static void m() {
		try {
			int x = 0;
			int y = 3/x;
			throw new RuntimeException("shouldn't be here");
		} catch (Throwable t) {
			return;
		}
	}
	
	public void m1(int x) {
		boolean b = true;
		if (b) {
			m();
		} else {
			m();
		}
	}
}

aspect A {
	before(Throwable t): handler(Throwable) && args(t) {
		System.out.println("caught " + t + " at " + thisJoinPointStaticPart);
	}
	
//	before(int i): cflow(execution(void m1(int)) && args(i)) && call(void m())  {
//		System.out.println("i");
//	}
}