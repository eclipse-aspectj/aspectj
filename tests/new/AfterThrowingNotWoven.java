import org.aspectj.testing.*;

public class AfterThrowingNotWoven {
    public static void main(String[] args) {
        try {
            new Server().doSomething();
        } catch (FaultException fe) {
            Tester.event("caught-in-main");
        }
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("caught");
        Tester.expectEvent("caught-in-main");
    }
}

class Server {
    public void doSomething() { 
        System.out.println("Doing something."); 
        throw new FaultException();
    }
} 

class DisabledException extends RuntimeException {}
class FaultException extends RuntimeException {}

aspect FaultHandler {

    private boolean Server.disabled = false;

    private void reportFault() {
        System.out.println("Failure! Please fix it.");
    }

    public static void fixServer(Server s) {
        s.disabled = false;
    }

    pointcut service(Server s): target(s) && call(public * *(..));

    before(Server s): service(s) {
        if (s.disabled) throw new DisabledException();
    }

    after(Server s) throwing (FaultException e): service(s) {
        s.disabled = true;
        Tester.event("caught");
    }
}
