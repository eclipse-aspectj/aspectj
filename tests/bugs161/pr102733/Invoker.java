import java.lang.reflect.Method;

public class Invoker {
	public static void main(String[] args) throws Throwable {
		try {
			C.main(null);
		} catch (Throwable t) {
			boolean failedCorrectly = t.toString().indexOf("Unresolved compilation")!=-1;
			if (failedCorrectly) return;
			throw t;
		}
		throw new RuntimeException("Call to main should have failed!");
	}
}