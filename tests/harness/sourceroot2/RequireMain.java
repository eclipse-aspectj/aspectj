
import org.aspectj.testing.Tester;

/** Make Tester require execution of main method */
public aspect RequireMain {
    static {
        Tester.expectEvent("RequireMain - main");
    }
    before() : execution(static void main(String[])) {
        Tester.event("RequireMain - main");
    }
}