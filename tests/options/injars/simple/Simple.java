

import org.aspectj.testing.Tester;

aspect Simple {
    before() : execution(void run()) {
        Tester.event("run");
    }
}