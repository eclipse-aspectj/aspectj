
import org.aspectj.testing.Tester;

import java.util.*;

// PR#304 lookup rules for unqualified pointcut names


public class Driver {

    public static String s = "";
    
    public static void main(String[] args){
        new MyObject().go();
        Tester.checkEqual(s, "-before-new", "");
    }
   
}

aspect MyPointCuts {
    pointcut excludes():
        (call(* equals(..)))    ||
        (call(* toString(..)))  ||
        (call(* hashCode(..)))  ||
        (call(* clone(..)))
        ;  
    pointcut allCalls(): call(* *(..)) && !excludes();
}

aspect MyAspect /*of eachobject(instanceof(MyObject))*/ {
    before(): MyPointCuts.allCalls() && target(MyObject) {
        Driver.s += "-before";
    }   
}

class MyObject {
    public void go() {
        Driver.s += "-new";
    }
}
