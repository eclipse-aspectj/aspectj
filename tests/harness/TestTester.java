
import org.aspectj.testing.Tester; 

public class TestTester {
    public static void main (String[] args) {
        Tester.event("1");
        Tester.note("note 1");
        Tester.note("note 2");
        int i = 1;
        Tester.check("note " + (i++));
        Tester.check("note " + (i++), "second note failed");
        new TestTester().run();
        Tester.checkAllEvents(); // does this empty 
        // now check(String[])
        Tester.clear();
        Tester.event("one");
        Tester.event("two");
        Tester.checkEvents(new String[] { "one", "two"}); 
    } 
    static {
        Tester.expectEvent("1");
        Tester.expectEvent("2");
    }
    public void run() {
        Tester.event("2");
        Tester.check(true, "no failure");
        Tester.checkEqual("1", "1", "no failure");
        Tester.checkEqual("1", "1");
    }
    
}
