import org.aspectj.testing.Tester;

public class TwoIntros {
    public static void main(String[] args) {
        Tester.check(false, "shouldn't compile!");
        
    }
}

class A {
}

aspect Aspect {
    int A.i;

    String A.i;
}
