import org.aspectj.testing.*;

public class ClosingBrace {
    public static void main(String[] args) {
        new ClosingBrace().realMain(args);
    }
    
    public void realMain(String[] args) {        
        Tester.check(false, "Shouldn't have compiled");
    }   
    {

