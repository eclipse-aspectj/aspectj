import org.aspectj.testing.Tester;

public class Proceeding1 {
    public static void main(String[] args) { 
       Tester.checkAllEvents();
    }
    static aspect A {
        interface IProceed {
            void proceeds(Runnable next);
        }
        IProceed decorator = new IProceed() {
            public void proceeds(Runnable next) {
                Tester.event("IProceed.proceed()");
                next.run();
            }
        };
        void around() : execution(void main(String[])) {
            Tester.expectEvent("IProceed.proceed()");
            decorator.proceeds(new Runnable() {
                public void run() {
                    proceed();
                }
            });
        }
    }
}