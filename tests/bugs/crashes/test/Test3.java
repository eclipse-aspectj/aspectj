package test;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public class Test3 {
   public static void main(String[] args) throws Exception {
      Test3 a = new Test3();
      a.foo(-3);
   }
   public void foo(int i) {
      this.x=i;
   }
   int x;
}

aspect Log {
   pointcut assign(Object newval, Object targ):
      set(* test..*)  && args(newval) && target(targ);

	
   before(Object newval, Object targ): assign(newval,targ) {
      Signature sign = thisJoinPoint.getSignature();
      System.out.println(targ.toString() + "." + sign.getName() + ":=" + newval);
   }
   /*
}
// Different error message if you divide into two aspects
aspect Tracing {
   */
   pointcut tracedCall():
      call(* test..*(..))/* && !within(Tracing)*/ && !within(Log);

   after() returning (Object o):  tracedCall() {
      // Works if you comment out either of these two lines
      thisJoinPoint.getSignature();
      System.out.println(thisJoinPoint);
   }
} 
