import org.aspectj.testing.*;

public class FinalStatic {
    public static void main(String[] args) {
        new FinalStatic().realMain(args);
    }
    
    public void realMain(String[] args) {        
        Tester.check(false, "Shouldn't have compiled");
    }

    final static int i = -1;
    { i = 13; }
}    
