import org.aspectj.testing.Tester;

public class TypeCoercions {
    public static void main(String[] args) {
        char c1 = 'e', c2 = 'f';
	Tester.checkEqual("hello".indexOf(c1), 1);
	Tester.checkEqual("hello".indexOf(c2), -1);
    }
}
