import org.aspectj.testing.Tester;

public class User1 {
	public static void main(String[] args) {
		Receiver r = new Receiver();
		Tester.check(r.someField, "introduced field");
	}
}