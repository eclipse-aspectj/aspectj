import org.aspectj.testing.*;

public class BadCast {
    public static void main(String[] args) {
        new BadCast().realMain(args);
    }
    
    public void realMain(String[] args) {
        int i = )int) 13;
        Tester.check(false, "shouldn't have compiled");
    }   
}
