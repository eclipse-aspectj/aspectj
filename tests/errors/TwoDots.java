import org.aspectj.testing.*;

public class TwoDots {
    public static void main(String[] args) {
        new TwoDots().realMain(args);
    }
    
    public void realMain(String[] args) {
        this..foo();
        //this..i = 14;
        Tester.check(false, "Shouldn't have compiled");
    }

    int i = 13;
    void foo() {}
}

