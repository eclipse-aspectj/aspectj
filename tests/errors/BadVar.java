import org.aspectj.testing.*;

public class BadVar {
    public static void main(String[] args) {
        new BadVar().realMain(args);
    }
    
    public void realMain(String[] args) {
        int _ _ = 13;
        Tester.check(false, "Shouldn't have compiled");
    }
}    
