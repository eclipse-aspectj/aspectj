import org.aspectj.testing.*;

public strictfp class StrictFp {
    public static void main(String[] args) {
        new StrictFp().go();
        Tester.check(ran, "go did not run");
    }

    static boolean ran = false;

    void go() {
        ran = true;
    }
}
