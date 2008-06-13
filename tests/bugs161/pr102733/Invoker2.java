import java.lang.reflect.Method;

public class Invoker2 {
	public static void main(String[] args) throws Throwable {
		C2.main(null); // C2.main() isnt broken but C2.foo() is
	}
}