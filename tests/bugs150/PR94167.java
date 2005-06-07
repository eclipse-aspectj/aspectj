package reflect;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

aspect Test {
    before() : call(* *(..)) && !within(Test) {
       MethodSignature sig = (MethodSignature)thisJoinPoint.getSignature();
       //sig.getDeclaringType(); // uncomment to work-around
       Method method = sig.getMethod();
   }
}

public class PR94167 {
    public static void main(String args[]) {
	try {
	    Inner.foo();
	} catch (Throwable t) {
	    t.printStackTrace();
	}
    }
    public static class Inner {
	public static void foo() {}
    }
}