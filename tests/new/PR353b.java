import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import org.aspectj.testing.*;
import java.util.*;

public class PR353b {

   public static void main(String[] args){
     new PR353b().go();
   }
   
   void go(){
       s.c = "E";  C   c = new E();   c.foo();
       s.c = "C";      c = new C();   c.foo();
       s.c = "E";  E   e = new E();   e.foo();
       s.c = "E2"; E2 e2 = new E2(); e2.foo();
       s.c = "F";  F   f = new F();   f.foo();      
   }

    static {
       Tester.expectEvent("call C");
       Tester.expectEvent("call E2");
    }
}

class C { void foo() {} }
class E extends C {}
class F extends E {} 
class E2 extends C  { void foo() {} }

class s { public static String c; }

aspect A {

    pointcut p3(): this(C) && call(* foo()) && !target(E);
    before(): p3() {
        Object target = thisJoinPoint.getTarget();
        JoinPoint.StaticPart sp = thisJoinPoint.getStaticPart();
        Signature sig = sp.getSignature();
        Class dt = sig.getDeclaringType();
        Tester.check(!(target instanceof E),
                     target.getClass().getName() + " instanceof E");
        Tester.event("call " + target.getClass().getName());
        Tester.check(dt == PR353b.class,
                     "dt != instanceof PR353b");
	Tester.check(!(target instanceof E),
                     "!instanceof E");
        String c = thisJoinPoint.getSignature().getDeclaringType().getName();
        Tester.check(s.c.equals(c), "p3: " + s.c + " != " + c);
    }  
}
