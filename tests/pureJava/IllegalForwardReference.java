import org.aspectj.testing.Tester;

public class IllegalForwardReference {
    public static void main(String[] args) {
        System.out.println(j + ", " + i);
        Tester.check(true, "compiled!");
    }

    static int j = i;
    static int i = 13;
}
