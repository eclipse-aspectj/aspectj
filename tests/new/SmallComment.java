import org.aspectj.testing.*;

public class SmallComment {
    public static void main(String[] args) {
        new SmallComment().realMain(args);
    }
    
    public void realMain(String[] args) {
        /**/
        Tester.check(true, "Compiled");
    }
}

