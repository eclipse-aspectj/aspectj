package test;

import org.aspectj.testing.*;

aspect Trace pertarget(target(test.Test)) {

    pointcut runs(): call(void run());
    
    before(): runs() {
        Tester.event("before");
    }
    after():  runs() {
        Tester.event("after");
    }
}
