
import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

/** @testcase from biojava: org/biojava/bio/dp/SimpleMarkovModel.java:384 */
public class ConstructorFlow {
    final Runnable runner; // remove final and compile succeeds
    Runnable nonfinal;
    String one;
    /** @testcase PUREJAVA flow analysis where final variable set in another constructor */
    public ConstructorFlow(String one, String two) {
        this(one);
        runner.run();  // incorrect CE: Field runner might not have a value
        nonfinal.run();  // expecting NPE
    }

    public ConstructorFlow(String one) {
        this.one = one;
        runner = new Runnable() { 
                public void run() {
                    Tester.event("runner.run()");
                }};
    }

    public static void main(String[] args) {
        Tester.expectEvent("NullPointerException");
        Tester.expectEvent("runner.run()");
        try {
            new ConstructorFlow("one", "two");
            Tester.check(false, "expected NPE");
        } catch (NullPointerException npe) {
            Tester.event("NullPointerException");
        }
        Tester.checkAllEvents();
    }
}

