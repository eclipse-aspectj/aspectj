import org.aspectj.testing.*;

public class BadValue {
    public static void main(String[] args) {
        new BadValue().realMain(args);
    }
    
    public void realMain(String[] args) {
        int i = ,;
        //String s = ,;
        Tester.check(false, "Shouldn't have compiled");
    }
}    
