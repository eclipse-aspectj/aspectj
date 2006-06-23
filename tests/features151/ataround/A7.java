// Bind the this on a call and change it with proceed... makes no difference
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A7 {
  N newN = new N();

  @Around("call(void M.method(String)) && args(p) && this(t)")
  public void a( ProceedingJoinPoint pjp, N t,String p) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed(new Object[]{newN,"faked"});
  }

  public static void main(String []argv) {
    N.main(argv);
  }
}

class N {
 public static void main( String[] args ) {
   N n = new N();
   n.methodCaller("real");
 }


 public void methodCaller(String param) {
   M m = new M("1");
   m.method(param);
 }

}


class M {
 String prefix;
 public M(String prefix) { this.prefix = prefix; }
 public void method(String s) { System.err.println(prefix+s); }
}

/*
class M {

 String prefix;

 public M(String prefix) { this.prefix = prefix; }

 public static void main( String[] args ) {
   M m = new M("1");
   m.methodCaller("real");
 }

 public void methodCaller(String param) {
   method(param);
 }

 public void method(String s) { System.err.println(prefix+s); }

}
*/
