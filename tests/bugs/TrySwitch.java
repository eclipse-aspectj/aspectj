public class TrySwitch {
	public static void main(String[] args) throws Throwable {
		m(10);
	}
	
	static boolean done = true;
	static int m(int i) {
		try {
			switch(i) {
				default: return 10;
				case 10:
					if (false) { 
						break;
					} else {
						throw new RuntimeException();
					}
				case 11: break;
			}
		} catch (Throwable e) {
			System.err.println("caught: " + e);
		}
		return 33;
	}
}


