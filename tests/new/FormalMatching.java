import org.aspectj.lang.*;
import org.aspectj.testing.*;

public class FormalMatching {
    public static void main(String[] args) {
        new FormalMatching().realMain(args);
    }
    public void realMain(String[] args) {
        call_v();
        call_vI(0);
        call_vII(0,1);
        call_i();
        call_iI(0);
        call_iII(0,1);
        Tester.checkAllEvents();
    }

    static String[] methods = {
        "call_v", "call_vI", "call_vII",
        "call_i", "call_iI", "call_iII",
    };
    static {
        for (int i = 0; i < methods.length; i++) {
            Tester.expectEvent(methods[i]);
            Tester.expectEvent(methods[i] + "-advice");
        }
    }
    
    void call_v  ()               { Tester.event("call_v");   }
    void call_vI (int i0)         { Tester.event("call_vI");  }
    void call_vII(int i0, int i1) { Tester.event("call_vII"); }

    int call_i  ()                { Tester.event("call_i");   return 0;  }
    int call_iI (int i0)          { Tester.event("call_iI");  return 0; }
    int call_iII(int i0, int i1)  { Tester.event("call_iII"); return 0; }
}

aspect Aspect {
    pointcut args0(Object o):                 call(* call*())      && target(o);
    pointcut args1(Object o, int i0):         call(* call*(int))    && target(o) && args(i0);
    pointcut args2(Object o, int i0, int i1): call(* call*(int,int)) && target(o) && args(i0, i1);    

    before(Object o): args0(o)                       { a(thisJoinPoint); }
    before(Object o, int i0): args1(o,i0)            { a(thisJoinPoint); }
    before(Object o, int i0, int i1): args2(o,i0,i1) { a(thisJoinPoint); }
    
    static void a(JoinPoint jp) {
        Tester.event(jp.getSignature().getName() + "-advice");
    }
}


