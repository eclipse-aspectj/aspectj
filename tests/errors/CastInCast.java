import org.aspectj.testing.*;

public class CastInCast {
    public static void main(String[] args) {
        new CastInCast().realMain(args);
    }
    
    public void realMain(String[] args) {
        int i = ((int) int) 13;
        Tester.check(false, "Shouldn't have compiled");
    }   
}
