import org.aspectj.testing.*;

public class Colon {
    public static void main(String[] args) {
        new Colon().realMain(args);
    }
    
    public void realMain(String[] args) {
        int i = 13:
        Tester.check(false, "Shouldn't have compiled");
    }
}

