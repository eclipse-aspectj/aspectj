
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class TestTesterFail {
    public static void main (String[] args) {
        Tester.event("1");
        Tester.event("event 1");
        new TestTesterFail().run();
        Tester.checkAllEvents(); // does not include events
        Tester.checkEventsFromFile("TestTester.events"); // should fail if FNF
    } 
    static {
        Tester.expectEvent("1");
        Tester.expectEvent("2");   // fail here - misentered below
        Tester.expectEvent("3");   // fail here - expected but not found
    }
    public void run() {
        Tester.event("2 "); // 
        Tester.event("event2"); // fail here - space
        Tester.check(false, "failure");  // fail here - explicitly
        Tester.checkEqual("1", "1 ", "failure"); // fail here - space
        Tester.checkEqual("", "1"); // fail here
    }
    
}
