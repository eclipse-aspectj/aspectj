import org.aspectj.testing.Tester;

public class AssertsCF {
    public static void main(String[] args) {
        int x = 0;
        boolean b;
        
        assert b; // ERR: b might not be assigned

        assert b=false;
        if (b) {} // ERR: b might not be assigned
    }
}
