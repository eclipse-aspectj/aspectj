import org.aspectj.testing.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.util.*;

public class SimpleAround1 {
    public static void main(String[] args) {
	new SimpleAround1().go();
        Tester.checkEqual(A.ran, "foo:goo:boo:", "advice didn't run");
    }
    void go() {
        foo("1");
	goo("2");
	boo("3");

    }
    void foo(String s) { new Integer(2).toString(); }

    void goo(String s) { }

    void boo(String s) { }
}

aspect A {

    void around(String s): execution(void *.*oo(String)) && args(s){
	proceed(s);
        JoinPoint jp = thisJoinPoint;
        ran += jp.getSignature().getName()+":";
    }

    static String ran = "";

    // When this advice is here no joinpoint is constructed in foo(String)
    before(): execution(void *.foo(String)) { }
    before(): execution(void *.goo(String)) {thisJoinPoint.getThis(); }

    before(): call(* Integer.*(..)) {thisJoinPoint.getThis(); }
}
