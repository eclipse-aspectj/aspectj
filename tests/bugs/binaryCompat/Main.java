public class Main {
	public static void main(String[] args) {
		try {
			doit();
			if (Trace.expectNoSuchMethodError) {
				throw new RuntimeException("expected NoSuchMethodError");
			} 
		} catch (NoSuchMethodError e) {
			if (!Trace.expectNoSuchMethodError) throw e;
		}
		
	}
	
	private static void doit() {
		System.out.println("hello world");
	}
}
