import org.aspectj.testing.Tester; 
public class AbstractAspectsExtendingAbstractAspectsGeneratesMethodsWithTheSameName_PR464 {
    public static void main(String[] args) {
        new AbstractAspectsExtendingAbstractAspectsGeneratesMethodsWithTheSameName_PR464().realMain(args);
    }
    public void realMain(String[] args) {
        new C().c();
        // No tests to actually run.  The point is that all advice is abstract!
        //Tester.checkAllEvents();
    }
    static {
        //Tester.expectEventsInString("AspectA.star,AspectA.c");
        //Tester.expectEventsInString("AspectB.star,AspectB.c");
        //Tester.expectEventsInString("AspectC.star,AspectC.c");
    }
}

class C {
    public void c() {}
}

abstract aspect AspectA {   
    before() : execution(* *(..)) {
        Tester.event("AspectA.star");
    }
    
    before() : execution(* c(..)) {
        Tester.event("AspectA.c");
    } 
}

abstract aspect AspectB extends AspectA {

    before() : execution(* *(..)) {
       Tester.event("AspectB.star");
    }

    before() : execution(* c(..)) {
        Tester.event("AspectB.c");
    }    
}

abstract aspect AspectC extends AspectB {
    before() : execution(* *(..)) {
        Tester.event("AspectC.star");
    }

    before() : execution(* c(..)) {
        Tester.event("AspectC.c");
    } 
}

