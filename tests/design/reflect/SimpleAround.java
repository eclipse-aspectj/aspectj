import org.aspectj.lang.reflect.*;

public class SimpleAround {
    public static void main(String[] args) {
	new SimpleAround().foo("hi");
    }

    void foo(String s) {
	System.out.println("foo(" + s + ")");
    }
}

aspect A {
    static around(String x) returns void: executions(void *.foo(x)) {
	System.out.println("calling foo with: " + x +", " + thisStaticJoinPoint);
	proceed(x);
	System.out.println("returning from: " + thisJoinPoint); //((CallJoinPoint)thisJoinPoint).getParameters()[0]);
    }

    static before(): executions(void *.foo(String)) {
    	System.out.println("entering: " + thisJoinPoint);
    }

    //static around() returns void: calls(void *.foo(String)) {
    //System.out.println(thisStaticJoinPoint);
    //proceed();
    //}


}
