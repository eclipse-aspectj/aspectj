import org.aspectj.testing.*;

public class Orleans {
    static boolean called = false;
    public static void main(String[] args) {
        String hello[] = new String[] { "h", "e", "l", "l", "o" };
	Object o = new Object();
	o = new Orleans(true);
        Tester.check(called, "Advice was not called.");
    }

    public Orleans(boolean b) {}
}

aspect ResourceAccounting issingleton() {
    pointcut constructions(): call(new(..));
    before(): constructions() {
        Orleans.called = true;
    }
}

    
