import org.aspectj.testing.*;

public class Const {
    public static void main(String[] args) {
        new Const().realMain(args);
    }
    
    public void realMain(String[] args) {
        int const = 2;
        int i = const * 2;
        Tester.check(false, "shouldn't have compiled");
    }

    public static void testme1() {
        int i = const + 1;
    }

    public static void testme2() {
        int j = Const.const + 1;
    }
}
