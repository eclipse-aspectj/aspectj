import org.aspectj.testing.*;

public class NoMethodName {
    public static void main(String[] args) {
        new NoMethodName().realMain(args);
    }
    
    public void realMain(String[] args) {
        System.out.();
        Tester.check(false, "Shouldn't have compiled");
    }
}    
