import org.aspectj.testing.*;

public class BindingNullPointer {
    boolean ran = false;    
    final String s1 = new String("s1");
    Runnable r1 = new Runnable() {            
            public void run() {String = s1; ran = true;}
        };
    void go() {
        r1.run();
        Tester.check(ran, "r1.run did not run");
    }

    public static void main(String[] args) {
        new BindingNullPointer().go();
    }
}
