import org.aspectj.testing.Tester;

public class Main {
	
	public static void main(String[] args) {
		Tester.checkFailed("Incremental compilation did not appear to (re)weave Main");
	}
		
}