import org.aspectj.testing.*;
public class Goto {
    public static void main(String[] args) {
        int goto = 13;
        Tester.check(false, "shouldn't have compiled");
    }
    
    public static void testme1() {
        int i = goto + 1;
        int j = Goto.goto + 1;
    }

    public static void testme2() {
        int j = Goto.goto + 1;
    }
}
