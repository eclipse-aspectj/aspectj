import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 
/*
 * Calls to methods declared in outer annonymous classes
 * are being qualified with this when they shouldn't.
 */
public class MisplacedThisInAnnonymousInnerClasses {
    public static void main(String[] args) {
        new MisplacedThisInAnnonymousInnerClasses().realMain(args);
    }
    public void realMain(String[] args) {

        Tester.expectEvent("run0");
        Tester.expectEvent("run1");
        Tester.expectEvent("run2");
        Tester.expectEvent("run3");
        Tester.expectEvent("outer0");
        Tester.expectEvent("outer1");
        Tester.expectEvent("outer2");
        
        new Runnable() {
                public void outer(int i) { Tester.event("outer"+i); }
                public void run() {
                    Tester.event("run0");
                    new Runnable() {
                            public void run() {
                                Tester.event("run1");
                                // shouldn't become this.outer(0)
                                outer(0);
                                new Runnable() {
                                        public void run() {
                                            Tester.event("run2");
                                            // shouldn't become this.outer(1)
                                            outer(1);
                                            new Runnable() {
                                                    public void run() {
                                                        Tester.event("run3");
                                                        // shouldn't become
                                                        // this.outer(2)
                                                        outer(2);
                                                    }
                                                }.run();
                                        }
                                    }.run();
                            }
                        }.run();
                }
            }.run();

        Tester.checkAllEvents();
    }

}
