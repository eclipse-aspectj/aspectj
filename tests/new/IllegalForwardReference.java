import org.aspectj.testing.Tester;

public class IllegalForwardReference {
    public static void main(String[] args) {
        System.out.println(new Aspect.InnerClass().j + ", " + new Aspect.InnerClass().i);
        Tester.check(true, "compiled!");
    }
}

aspect Aspect {
    //int InnerClass.i = 13;

    static class InnerClass {
        int j = i;
        int i = 13;
    }
}
