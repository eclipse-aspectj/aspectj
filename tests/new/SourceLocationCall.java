
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;

/** @testcase PR#658 simple call join point tests for JoinPoint SourceLocation context */
public class SourceLocationCall {
    public static void main(String[] args) {
        new SourceLocationCall().maincall();
    }
    public void maincall(){}
}

aspect Tracing {
    static void check(String label, JoinPoint jp, JoinPoint.StaticPart sp) {
//          System.err.println("checking " + label + " " + jp + " " + sp 
//                             + " - " + jp.getSourceLocation() 
//                             + " - " + sp.getSourceLocation() );
        if (null == jp) {
            Tester.check(false, "null JoinPoint@" + label);
        } else {
            Tester.check(null != jp.getSourceLocation(), 
                         "null jp source location@" + label);
        }
        if (null == sp) {
            Tester.check(false, "null JoinPoint.StaticPart@"+label);
        } else {
            Tester.check(null != sp.getSourceLocation(), 
                         "null sp source location@" + label);
        }
    }
    pointcut trace1()
        : call(void SourceLocationCall.maincall(..)); 
    // ok
    //: within(SourceLocationCall) ;
    //: cflow(execution(void SourceLocationCall.main(String[]))) && !within(Tracing) ;
    //: within(SourceLocationCall) && call(* *(..));
    //: execution(* SourceLocationCall.*(..));
    //: call(void SourceLocationCall.main*(..)) && within(*);
    // fail
    //: call(void SourceLocationCall.main*(..));
    //: call(* SourceLocationCall.*(..));
    //: call(void SourceLocationCall.*(..));
    // same result for static calls and instance calls
    // same result for before and after
    before() : trace1() {
        check("before() : trace1()", thisJoinPoint, thisJoinPointStaticPart);
    }
    after() : trace1() {
        check("after() : trace1()", thisJoinPoint, thisJoinPointStaticPart);
    }

    before(): call(void SourceLocationCall.main(..)) {
        Tester.check(thisJoinPoint.getSourceLocation() == null, "main call");
        Tester.check(thisJoinPoint.getThis() == null, "main call");
    }
}


