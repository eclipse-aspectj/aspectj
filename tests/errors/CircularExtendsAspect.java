import org.aspectj.testing.Tester;

/**
 * @errors 18
 * @warnings
 */

public class CircularExtendsAspect {
    public static void main(String[] args) {
        new CircularExtendsAspect().realMain(args);
    }
    
    public void realMain(String[] args) {
        Tester.check(false, "shouldn't have compiled");
    }
}

aspect Aspect extends Aspect {
}
