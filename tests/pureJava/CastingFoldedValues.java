import org.aspectj.testing.Tester;

public class CastingFoldedValues {
    static final char i = 'c';

    static boolean foundChar = false;

    static void foo(char c) { foundChar = true; }
    static void foo(int c) {  }

    public static void main(String[] args) {
	foo(i);
	Tester.check(foundChar, "forgot to cast folded char down to char type");
    }
}
