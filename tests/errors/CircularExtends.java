import org.aspectj.testing.Tester;

/**
 * @errors 7
 * @warnings
 */
public class CircularExtends extends CircularExtends {
    public static void main(String[] args) {
        new CircularExtends().realMain(args);
    }
    
    public void realMain(String[] args) {
        Tester.check(false, "shouldn't have compiled");
    }
}
