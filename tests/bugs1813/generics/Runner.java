public class Runner {
	public static void main(String[] argv) {
		if (new AlreadyImplementsA() instanceof A) {
			System.out.println("ok");
		}
	}
}
