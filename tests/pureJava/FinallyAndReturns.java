import org.aspectj.testing.Tester;

public class FinallyAndReturns {
    public static void main(String[] args) {
        Tester.checkEqual(m(), "hi");
        Tester.check("finally");

        Tester.checkEqual(m1(), "hi1");
        Tester.check("trying");
    }

    public static String m() {
        try {
            return "hi";
        } finally {
            Tester.note("finally");
        }
    }

    public static String m1() {
        try {
            Tester.note("trying");
        } finally {
            return "hi1";
        }
    }

}
