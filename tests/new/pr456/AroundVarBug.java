import java.util.*;
import org.aspectj.testing.Tester; 

/** @testcase PR#456 advice on advice in usejavac mode */
public aspect AroundVarBug {
    static {
        Tester.expectEvent("Meta-advice reached");
        Tester.expectEvent("advice reached");
    }
 
    protected pointcut iterator () :
        call (public Iterator Collection.iterator ());
 
    Iterator around () : iterator () {
        return handleIterator (thisJoinPoint, proceed ());
    }
 
    private Iterator handleIterator (Object join, Iterator iter) {
        Tester.event("advice reached");
        return iter;
    }
 
    // -------------
    //  Meta-advice
    // -------------
 
    private pointcut adviceHandlers (): 
        call (private * AroundVarBug.handle*(..));
 
    // Advice on this aspect!
    Object around () : adviceHandlers () {
        Tester.event("Meta-advice reached");
        return proceed();
    }
 
} // end of aspect AroundVarBug


