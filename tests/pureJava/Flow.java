import org.aspectj.testing.Tester;

public class Flow {
    public static void main(String[] args) {
        Tester.checkEqual(m(), 42, "m()");
        Tester.checkEqual(n(), 42, "n()");
    }

    static int m() {
        if (true) return 42;
        return 3;
    }

    static int n() {
        { if(true) return 42; }
        return 4;
    }
}
