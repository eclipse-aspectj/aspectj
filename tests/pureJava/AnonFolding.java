import org.aspectj.testing.Tester;

public class AnonFolding {
    public static void main(String[] args) {
        Tester.checkEqual(ANON.toString(), "ANON");
    }

    public static Object ANON = new Object() {
            public Object m() { return ANON; }
            public String toString() {
                Tester.checkEqual(ANON, m(), "reference to itself");
                return "ANON";
            }
                
        };
}
