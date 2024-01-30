

package language;

import org.aspectj.testing.Tester;

/** @author Wes Isberg */
public class DoubleDispatch {

    public static void main(String[] a) {
        Worker worker = new Worker();
        worker.run((SuperType) new SubTypeOne());
        worker.run((SuperType) new SubTypeTwo());
        worker.run(new SuperType());
        Tester.checkAllEvents();
    }
    static aspect A {
        static int counter;
        static {
            Tester.expectEvent("language.SubTypeOne-1");
            Tester.expectEvent("language.SubTypeTwo-2");
            Tester.expectEvent("language.SuperType-3");
        }
        before(Object o) : execution(void Worker.run(..)) && args(o) {
            Tester.event(o.getClass().getName() + "-" + ++counter);
        }
    }
}

// START-SAMPLE language-doubleDispatch Implementing double-dispatch 

/**
 * By hypothesis, there is a working class with
 * methods taking a supertype and subtypes. 
 * The goal of double-dispatch is to execute the
 * subtype method rather than the supertype 
 * method selected when the compile-time
 * reference is of the super's type.
 */
class Worker {
    void run(SuperType t) {}
    void run(SubTypeOne t) {}
    void run(SubTypeTwo t) {}
}

class SuperType {}
class SubTypeOne extends SuperType {}
class SubTypeTwo extends SuperType {}

/** Implement double-dispatch for Worker.run(..) */
aspect DoubleDispatchWorker {

    /** 
     * Replace a call to the Worker.run(SuperType)
     * by delegating to a target method.  
     * Each target subtype in this method dispatches back 
     * to the subtype-specific Worker.run(SubType..) method,
     * to implement double-dispatch.
     */
    void around (Worker worker, SuperType targ):
           !withincode(void SuperType.doWorkerRun(Worker)) &&
            target (worker) && call (void run(SuperType)) &&
            args (targ) {
        targ.doWorkerRun(worker);
    }

    void SuperType.doWorkerRun(Worker worker) {
        worker.run(this);
    }

    // these could be in separate aspects
    void SubTypeOne.doWorkerRun(Worker worker) {
        worker.run(this);
    }
    void SubTypeTwo.doWorkerRun(Worker worker) {
        worker.run(this);
    }
}

// END-SAMPLE language-doubleDispatch 
 