import org.aspectj.testing.Tester;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class NewSiteAdvice {

    public static void main(String[] args) { test(); }

    NewSiteAdvice() throws RemoteException { }

    /* here's another comment */
    public static void test() {
        Tester.checkEqual(new NewSiteAdvice().go(), "ran", "rmi exception intercepted");
        Tester.check(new Integer(42) == A.cached42, "caching new 42");
        Tester.check("around NewSiteAdvice");
    }

    /** this is the way to make things go **/
    String go() {
        return "ran"; // and an eol commment
    }
}
aspect A {
    pointcut makeNewSiteAdvice():
        within(NewSiteAdvice) && call(NewSiteAdvice.new());
        
    declare soft: RemoteException: makeNewSiteAdvice();

    NewSiteAdvice around(): makeNewSiteAdvice() {
        NewSiteAdvice result = null;
        try {
            result = proceed();
        } catch (RemoteException e){
        }

        Tester.note("around NewSiteAdvice");
        return result;
    }

    Integer around(int i):
        args(i) && call(Integer.new(int)) && !within(A) {
        if (i == 42) return cached42;
        return proceed(i);
    }


    static Integer cached42 = new Integer(42);
}

