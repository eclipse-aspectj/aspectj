
import org.aspectj.testing.Tester;

public class TestTesterFile {
    public static void main (String[] args) {
        Tester.event("event 1");  // in TestTester.events
        new TestTesterFile().run();
        Tester.checkEventsFromFile("TestTester.events"); 
    } 
    public void run() {
        Tester.event("event 2");  // in TestTester.events
    }
    
}
